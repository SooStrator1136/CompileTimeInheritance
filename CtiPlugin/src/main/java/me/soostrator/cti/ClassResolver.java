package me.soostrator.cti;

import lombok.Getter;
import me.soostrator.cti.plugin.ResolverPlugin;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SooStrator1136
 */
final class ClassResolver {

    @Getter
    private final List<ClassNode> classNodes = new ArrayList<>(10);

    @Getter
    private final Map<ClassNode, String> jarLocations = new HashMap<>(10);

    @Getter
    private final Map<String, Collection<String>> classInfo = new HashMap<>(2);

    ClassResolver(final Iterable<byte[]> classes) {
        for (final byte[] classBytes : classes) {
            try {
                final ClassNode node = Utilities.toNode(classBytes);
                this.classNodes.add(node);
                this.jarLocations.put(node, node.name + ".class");
            } catch (final Throwable t) {
                if (!ResolverPlugin.CONFIG.isSkipFailingClasses()) {
                    //noinspection ProhibitedExceptionThrown
                    throw t;
                }
            }
        }

        //Running after all class nodes have been added since we need them for resolving
        for (final ClassNode classNode : this.classNodes) {
            this.classInfo.put(classNode.name, ClassInfo.superClasses(classNode, this));
        }
    }

    List<String> resolveAllHeirs(final String superClass) { //TODO not all are being resolved wtf
        final List<String> resolvedHeirs = new ArrayList<>(1);
        for (final ClassNode classNode : this.classNodes) {
            if (this.classInfo.get(classNode.name).contains(superClass)) {
                resolvedHeirs.add(classNode.name);
            }
        }

        return resolvedHeirs;
    }

}
