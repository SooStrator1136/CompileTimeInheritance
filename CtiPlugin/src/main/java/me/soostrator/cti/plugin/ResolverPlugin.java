package me.soostrator.cti.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The gradle plugin class, registers the {@link  ResolverExtension} and makes the {@link ResolveJarTask} available.
 *
 * @author SooStrator1136
 */
@SuppressWarnings("unused")
public final class ResolverPlugin implements Plugin<Project> {

    public static final ResolverExtension CONFIG = new ResolverExtension();

    @Override
    public void apply(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") final Project project) {
        project.getExtensions().add("resolver", CONFIG);
    }

}
