package me.soostrator.cti;

import me.soostrator.cti.plugin.ResolverPlugin;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author SooStrator1136
 */
@SuppressWarnings("HardcodedFileSeparator")
public class Resolver {

    private static final Pattern FILE_EXTENSION = Pattern.compile("[.][^.]+$");

    @SuppressWarnings("CallToPrintStackTrace")
    public static boolean resolveJar(final File in, final File out) {
        final Map<String, byte[]> editedEntries = new HashMap<>(4);

        try (final JarFile jar = new JarFile(in)) {
            final ClassResolver resolver = new ClassResolver(Utilities.getClassBytes(jar));

            for (final ClassNode classNode : resolver.getClassNodes()) {
                boolean wasModified = false;
                for (final Object methodNodeRaw : classNode.methods) {
                    wasModified = wasModified || checkModifyMethod((MethodNode) methodNodeRaw, resolver);
                }

                if (wasModified) {
                    editedEntries.put(resolver.getJarLocations().get(classNode), Utilities.toBytes(classNode));
                }
            }

            Utilities.saveToJar(editedEntries, in, out);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return !editedEntries.isEmpty();
    }

    public static boolean resolveClasses(final File startDirectory) {
        final Map<File, byte[]> editedClasses = new HashMap<>(4);

        final List<File> classFiles = Utilities.getSubFiles(startDirectory);

        if (ResolverPlugin.CONFIG.isResolveFoundJars()) {
            for (final File file : classFiles) {
                if (!file.getName().endsWith(".jar")) continue;

                Resolver.resolveJar(
                        file,
                        new File(FILE_EXTENSION.matcher(file.getAbsolutePath()).replaceFirst("") + ResolverPlugin.CONFIG.getJarExtension() + ".jar")
                );
            }
        }

        //Remove non-class files
        classFiles.removeIf(file -> !file.getName().endsWith(".class"));

        final Map<String, File> classLocations = new HashMap<>(classFiles.size());

        for (final File file : classFiles) {
            classLocations.put(
                    file.getAbsolutePath().substring(
                            startDirectory.getAbsolutePath().length() + 1
                    ).replaceAll(Pattern.quote(File.separator), "/"),
                    file
            );
        }

        try {
            final ClassResolver resolver = new ClassResolver(Utilities.getFilesBytes(classFiles));

            for (final ClassNode classNode : resolver.getClassNodes()) {
                boolean wasModified = false;

                for (final Object methodNodeRaw : classNode.methods) {
                    wasModified = wasModified || checkModifyMethod((MethodNode) methodNodeRaw, resolver);
                }

                if (wasModified) {
                    editedClasses.put(
                            classLocations.get(resolver.getJarLocations().get(classNode)),
                            Utilities.toBytes(classNode)
                    );
                }
            }
        } catch (final IOException e) {
            e.printStackTrace(); //Logging
        }

        for (final Map.Entry<File, byte[]> entry : editedClasses.entrySet()) {
            System.out.println(entry);
            try (final FileOutputStream out = new FileOutputStream(entry.getKey())) {
                out.write(entry.getValue());
            } catch (final IOException e) {
                e.printStackTrace(); //logging
            }
        }


        return !editedClasses.isEmpty();
    }

    private static boolean checkModifyMethod(final MethodNode toModify, final ClassResolver resolver) {
        boolean isModified = false;

        final int instructionsAmount = toModify.instructions.size();
        for (int i = 0; i < instructionsAmount; i++) {
            final AbstractInsnNode abstractInsnNode = toModify.instructions.get(i);

            if (abstractInsnNode.getType() == 5) {
                final MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;

                if (methodInsnNode.name.contentEquals("resolveAllHeirs") && methodInsnNode.owner.contentEquals("me/soostrator/cti/InheritanceResolver") && methodInsnNode.desc.contentEquals("(Ljava/lang/Class;)[Ljava/lang/Class;")) {
                    isModified = true;

                    AbstractInsnNode prevNode = methodInsnNode;

                    LdcInsnNode superClassNode = null;

                    int iterations = 0;
                    while (superClassNode == null) {
                        prevNode = prevNode.getPrevious();
                        iterations++;

                        if (prevNode instanceof LdcInsnNode) {
                            superClassNode = (LdcInsnNode) prevNode;
                        }
                    }

                    final String superClass = ((Type) superClassNode.cst).getInternalName();

                    //noinspection ReuseOfLocalVariable
                    prevNode = iterations == 1 ? methodInsnNode.getPrevious().getPrevious() : methodInsnNode.getPrevious();

                    //Remove the 2 call instructions
                    toModify.instructions.remove(superClassNode);
                    toModify.instructions.remove(methodInsnNode);

                    final List<String> heirs = resolver.resolveAllHeirs(superClass);
                    final int heirsAmount = heirs.size();

                    //Array setup
                    if (heirsAmount < 6) {
                        toModify.instructions.insert(prevNode, prevNode = new InsnNode(Opcodes.ICONST_0 + heirsAmount));
                    } else {
                        toModify.instructions.insert(prevNode, prevNode = new IntInsnNode(Opcodes.BIPUSH, heirsAmount));
                    }
                    toModify.instructions.insert(prevNode, prevNode = new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));

                    for (int index = 0; index < heirsAmount; index++) {
                        final String heir = heirs.get(index);
                        toModify.instructions.insert(prevNode, prevNode = new InsnNode(Opcodes.DUP));

                        if (index < 6) {
                            toModify.instructions.insert(prevNode, prevNode = new InsnNode(Opcodes.ICONST_0 + index));
                        } else {
                            toModify.instructions.insert(prevNode, prevNode = new IntInsnNode(Opcodes.BIPUSH, index));
                        }

                        toModify.instructions.insert(prevNode, prevNode = new LdcInsnNode(Type.getType("L" + heir + ";")));
                        toModify.instructions.insert(prevNode, prevNode = new InsnNode(Opcodes.AASTORE));
                    }

                    //noinspection AssignmentToForLoopParameter
                    i += heirsAmount << 2; //Aka heirsAmount * 4, we ignore the 2 setup calls (array size and newArray) because we also removed 2 instructions previously
                }
            }
        }

        return isModified;
    }

}
