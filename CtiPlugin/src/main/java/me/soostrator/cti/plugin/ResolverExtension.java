package me.soostrator.cti.plugin;

import lombok.Getter;
import lombok.Setter;

/**
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

}
