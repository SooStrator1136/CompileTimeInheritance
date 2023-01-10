package me.soostrator.cti;

import me.soostrator.cti.plugin.ResolverPlugin;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author SooStrator1136
 */
@SuppressWarnings("HardcodedFileSeparator")
class ClassInfo {

    private static final Pattern SLASH = Pattern.compile("/");
    private static final Pattern DOT = Pattern.compile("\\.");

    static Collection<String> superClasses(final ClassNode classNode, final ClassResolver resolver) {
        final List<String> superClasses = new ArrayList<>(1);

        superClasses.add(classNode.superName);
        if (classNode.interfaces != null) {
            for (final Object interfaceRaw : classNode.interfaces) {
                superClasses.add((String) interfaceRaw);
            }
        }

        //noinspection MethodCallInLoopCondition
        for (int i = 0; i < superClasses.size(); i++) { //TODO add option to only allow direct super classes
            if (ResolverPlugin.CONFIG.isOnlyDirectParents()) break;

            final String superClass = superClasses.get(i);
            @SuppressWarnings("BooleanVariableAlwaysNegated")
            boolean found = false;

            //noinspection ParameterNameDiffersFromOverriddenParameter
            for (final ClassNode node : resolver.getClassNodes()) {
                if (node.name.contentEquals(superClass)) {
                    superClasses.addAll(superClasses(node, resolver));
                    found = true;
                    break;
                }
            }

            if (!found && ResolverPlugin.CONFIG.isUseClassForName()) {
                try {
                    superClasses.addAll(superClasses(Class.forName(stringToClass(superClass))));
                } catch (final ClassNotFoundException ignored) {
                }
            }
        }

        return new HashSet<>(superClasses); //Remove duplicates
    }

    private static Collection<String> superClasses(final Class<?> startClass) {
        final List<String> superClasses = new ArrayList<>(1);

        superClasses.add(classToString(startClass.getSuperclass()));

        for (final Class<?> clazz : startClass.getInterfaces()) {
            superClasses.add(classToString(clazz));
        }

        superClasses.removeIf(Objects::isNull);

        //noinspection MethodCallInLoopCondition
        for (int i = 0; i < superClasses.size(); i++) {
            try {
                superClasses.addAll(superClasses(Class.forName(stringToClass(superClasses.get(i)))));
            } catch (final ClassNotFoundException ignored) {
            }
        }

        return superClasses;
    }

    private static String stringToClass(final CharSequence clazz) {
        return SLASH.matcher(clazz).replaceAll(".");
    }

    @SuppressWarnings("ReturnOfNull")
    private static String classToString(final Class<?> clazz) {
        if (clazz == null) return null;

        return DOT.matcher(clazz.getName()).replaceAll("/");
    }

}
