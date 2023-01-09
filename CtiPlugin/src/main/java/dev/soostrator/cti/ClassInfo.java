package dev.soostrator.cti;

import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SooStrator1136
 */
class ClassInfo {

    @Getter
    private final List<String> superClasses = new ArrayList<>(1);

    ClassInfo(final ClassNode classNode, final ClassResolver resolver) {
        superClasses.add(classNode.superName);
        if (classNode.interfaces != null) {
            for (final Object interfaceRaw : classNode.interfaces) {
                superClasses.add((String) interfaceRaw);
            }
        }
    }

}
