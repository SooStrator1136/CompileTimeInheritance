package dev.soostrator;

import dev.soostrator.cti.Resolver;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author SooStrator1136
 */
public class Main {

    private static final Pattern FILE_EXTENSION = Pattern.compile("[.][^.]+$");

    public static void main(final String[] args) {
        final File testFile = new File("C:\\Users\\wallm\\IdeaProjects\\PluginTest\\build\\libs\\PluginTest-1.0-SNAPSHOT.jar");

        Resolver.resolveAll(testFile, new File(FILE_EXTENSION.matcher(testFile.getAbsolutePath()).replaceFirst("") + "-resolved.jar"));
    }

}