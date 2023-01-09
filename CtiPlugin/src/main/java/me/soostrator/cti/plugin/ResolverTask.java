package me.soostrator.cti.plugin;

import me.soostrator.cti.Resolver;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author SooStrator1136
 */
@Getter
@Setter
public class ResolverTask extends DefaultTask {

    @InputFile
    private File inputJar;

    @OutputFile
    private File outputJar;

    @TaskAction
    public final void resolve() {
        if (this.inputJar == null || this.outputJar == null) {
            this.setDidWork(false);
            return;
        }
        this.setDidWork(Resolver.resolveAll(this.inputJar, this.outputJar));
    }

}
