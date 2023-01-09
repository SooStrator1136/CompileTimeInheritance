package me.soostrator.cti;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author SooStrator1136
 */
final class Utilities {

    private static final byte[] EMPTY_BYTES = new byte[0];

    static byte[] bytesOfInputStream(final InputStream in) {
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
        } catch (final IOException e) {
            e.printStackTrace(); //TODO actual logging
        }

        return bytes;
    }

}
