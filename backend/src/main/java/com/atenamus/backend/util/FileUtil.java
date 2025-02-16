package com.atenamus.backend.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

    /**
     * Writes the given byte array to a specified file.
     *
     * @param outputFilePath The path of the file where the data will be written.
     * @param data           The byte array to write to the file.
     * @throws IOException If an I/O error occurs during the file write operation.
     */
    public static void writeFile(String outputFilePath, byte[] data) throws IOException {
        // Use try-with-resources to ensure OutputStream is closed automatically
        try (OutputStream outputStream = new FileOutputStream(outputFilePath)) {
            outputStream.write(data);
            outputStream.close();
        }
    }
}
