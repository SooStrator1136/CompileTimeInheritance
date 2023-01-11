package me.soostrator.cti.plugin;

import lombok.Getter;
import lombok.Setter;

/**
 * Gradle extension used to configure the resolution process.
 *
 * @author SooStrator1136
 */
@Getter
@Setter
@SuppressWarnings("all")
public class ResolverExtension {

    /**
     * Whether to allow the usage of {@link Class#forName(String)} in the process of inheritance resolution or not,
     * which makes the resolution of standard java classes possible.
     * <p>
     * Default value: true
     */
    private boolean useClassForName = true;

    /**
     * Default value: false
     */
    private boolean onlyDirectParents = false;

    /**
     * Makes the {@link ResolveDirectoryTask} resolve jars in the given directory or its sub directories.
     *
     * Default value: true
     */
    private boolean resolveFoundJars = true;

    /**
     * If {@link ResolverExtension#isResolveFoundJars()} the given extension will be added to jars.
     *
     * Default value: -resolved
     */
    private String jarExtension = "-resolved";

    /**
     * Skips classes that asm fails to read, when false the exception raised by asm is thrown, when false the exception is ignored.
     *
     * Default value: true
     */
    private boolean skipFailingClasses = true;

}
