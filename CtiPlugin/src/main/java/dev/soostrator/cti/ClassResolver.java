package dev.soostrator.cti;

import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author SooStrator1136
 */
final class ClassResolver {

    @Getter
    private final List<ClassNode> classNodes = new ArrayList<>(10);

    @Getter
    private final Map<ClassNode, String> jarLocations = new HashMap<>(10);

    @Getter
    private final Map<String, ClassInfo> classInfo = new HashMap<>(2);

    ClassResolver(final JarFile jar) {
        final Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            final JarEntry currentEntry = entries.nextElement();

            if (!currentEntry.getName().endsWith(".class")) continue;

            try {
                final ClassNode node = toNode(Utilities.bytesOfInputStream(jar.getInputStream(currentEntry)));
                this.classNodes.add(node);
                this.jarLocations.put(node, currentEntry.getName());
            } catch (final IOException e) {
                e.printStackTrace(); //TODO logging oml
            }
        }

        for (final ClassNode classNode : this.classNodes) {
            this.classInfo.put(classNode.name, new ClassInfo(classNode, this));
        }

        try {
            jar.close();
        } catch (final IOException e) {
            e.printStackTrace(); //Logging
        }
    }

    List<String> resolveAllHeirs(final String superClass) {
        final List<String> resolvedHeirs = new ArrayList<>(1);

        for (final ClassNode classNode : this.classNodes) {
            if (this.classInfo.get(classNode.name).getSuperClasses().contains(superClass)) {
                resolvedHeirs.add(classNode.name);
            }
        }

        return resolvedHeirs;
    }

    private static ClassNode toNode(final byte[] bytes) {
        final ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
        return node;
    }

}
