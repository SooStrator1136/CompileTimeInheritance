package me.soostrator.cti.plugin;

import lombok.Getter;
import lombok.Setter;
import me.soostrator.cti.Resolver;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author SooStrator1136
 */
@Getter
@Setter
public class ResolveDirectoryTask extends DefaultTask {

    @InputDirectory
    private File inputDirectory;

    @TaskAction
    public final void resolve() {
        if (this.inputDirectory == null) {
            this.setDidWork(false);
            return;
        }
        this.setDidWork(Resolver.resolveClasses(this.inputDirectory));
    }

}
