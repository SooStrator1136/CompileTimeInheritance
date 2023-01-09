package me.soostrator.cti;

/**
 * @author SooStrator1136
 */
public final class InheritanceResolver {

    private static final Class<?>[] CLASSES = {};

    /**
     * This will be replaced by an array of all classes that inherit from the given class at compile time.
     * This works for both interfaces and normal classes.
     *
     * @param from The superclass of which the heirs will be found of.
     * @return ALl the heirs.
     */
    public static Class<?>[] resolveAllHeirs(final Class<?> from) {
        return CLASSES;
    }

}
