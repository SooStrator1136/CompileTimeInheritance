package me.soostrator.cti;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author SooStrator1136
 */
final class Utilities {

    private static final byte[] EMPTY_BYTES = new byte[0];

    private static byte[] bytesOfInputStream(final InputStream in) {
        byte[] bytes = EMPTY_BYTES;

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            //noinspection CheckForOutOfMemoryOnLargeArrayAllocation
            final byte[] buffer = new byte[2048];
            int bytesRead;

            //noinspection MethodCallInLoopCondition
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            bytes = out.toByteArray();
            in.close();
        } catch (final IOException e) {
            e.printStackTrace(); //TODO actual logging
        }

        return bytes;
    }

    static ClassNode toNode(final byte[] bytes) {
        final ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
        return node;
    }

    static List<byte[]> getClassBytes(final JarFile jar) throws IOException {
        final List<byte[]> classBytes = new ArrayList<>(2);

        final Enumeration<JarEntry> jarEntries = jar.entries();

        while (jarEntries.hasMoreElements()) {
            final JarEntry currentEntry = jarEntries.nextElement();

            //Skip non-class entries
            if (!currentEntry.getName().endsWith(".class")) continue;

            classBytes.add(Utilities.bytesOfInputStream(jar.getInputStream(currentEntry))); //Stream is closed by bytesOfInputStream
        }

        jar.close();
        return classBytes;
    }

    @SuppressWarnings({"NestedTryStatement", "CallToPrintStackTrace"})
    static void saveToJar(final Map<String, byte[]> editedEntries, final File sourceJar, final File outJar) {
        try (final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outJar.toPath(), StandardOpenOption.CREATE))) {
            try (final ZipFile zip = new ZipFile(sourceJar)) {
                final Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    final ZipEntry currentEntry = entries.nextElement();

                    out.putNextEntry(new ZipEntry(currentEntry.getName()));

                    if (editedEntries.containsKey(currentEntry.getName())) {
                        out.write(editedEntries.get(currentEntry.getName()));
                    } else {
                        out.write(Utilities.bytesOfInputStream(zip.getInputStream(currentEntry)));
                    }

                    out.closeEntry();
                }

            } catch (@SuppressWarnings("OverlyBroadCatchBlock") final IOException e) {
                e.printStackTrace();
            }

            out.closeEntry();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    static List<File> getSubFiles(final File startDirectory) {
        final List<File> files = new ArrayList<>(2);

        final File[] subFiles = startDirectory.listFiles();

        if (subFiles == null) return files;

        //noinspection ParameterNameDiffersFromOverriddenParameter
        for (final File entry : subFiles) {
            if (entry.isDirectory()) {
                files.addAll(getSubFiles(entry));
            } else {
                files.add(entry);
            }
        }

        return files;
    }

    static List<byte[]> getFilesBytes(final Collection<? extends File> files) throws IOException {
        final List<byte[]> bytes = new ArrayList<>(files.size());

        for (final File file : files) {
            bytes.add(Files.readAllBytes(file.toPath()));
        }

        return bytes;
    }

    static byte[] toBytes(final ClassNode classNode) {
        final ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

}
