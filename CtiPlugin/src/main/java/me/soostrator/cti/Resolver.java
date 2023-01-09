package me.soostrator.cti;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author SooStrator1136
 */
@SuppressWarnings("HardcodedFileSeparator")
public class Resolver {

    @SuppressWarnings({"CallToPrintStackTrace", "BooleanMethodNameMustStartWithQuestion"})
    public static boolean resolveAll(final File in, final File out) {
        final Map<String, byte[]> editedEntries = new HashMap<>(4);

        try {
            final ClassResolver resolver = new ClassResolver(new JarFile(in));

            for (final ClassNode classNode : resolver.getClassNodes()) {
                boolean wasModified = false;
                for (final Object methodNodeRaw : classNode.methods) {
                    wasModified = wasModified || checkModifyMethod((MethodNode) methodNodeRaw, resolver);
                }

                if (wasModified) {
                    final ClassWriter classWriter = new ClassWriter(0);
                    classNode.accept(classWriter);
                    editedEntries.put(resolver.getJarLocations().get(classNode), classWriter.toByteArray());
                }
            }

            saveToJar(editedEntries, in, out);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return !editedEntries.isEmpty();
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

                    final String superClass = ((Type) ((LdcInsnNode) methodInsnNode.getPrevious()).cst).getInternalName();

                    AbstractInsnNode prevNode = methodInsnNode.getPrevious().getPrevious();

                    //Remove the 2 call instructions
                    toModify.instructions.remove(methodInsnNode.getPrevious());
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

    @SuppressWarnings({"NestedTryStatement", "CallToPrintStackTrace"})
    private static void saveToJar(final Map<String, byte[]> editedEntries, final File sourceJar, final File outJar) {
        try (final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outJar.toPath(), StandardOpenOption.CREATE))) {
            try (final ZipFile zip = new ZipFile(sourceJar)) {
                final Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    final ZipEntry currentEntry = entries.nextElement();

                    out.putNextEntry(new ZipEntry(currentEntry.getName()));

                    if (editedEntries.containsKey(currentEntry.getName())) {
                        out.write(editedEntries.get(currentEntry.getName()));
                    } else {
                        out.write(Utilities.bytesOfInputStream(zip.getInputStream(currentEntry)));
                    }

                    out.closeEntry();
                }

            } catch (@SuppressWarnings("OverlyBroadCatchBlock") final IOException e) {
                e.printStackTrace();
            }

            out.closeEntry();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
