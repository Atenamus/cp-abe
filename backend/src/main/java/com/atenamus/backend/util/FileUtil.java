package com.atenamus.backend.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.io.InputStream;

public class FileUtil {

    /**
     * Writes the given byte array to a specified file.
     *
     * @param outputFilePath The path of the file where the data will be written.
     * @param data           The byte array to write to the file.
     * @throws IOException If an I/O error occurs during the file write operation.
     */
    public static void writeFile(String outputFilePath, byte[] data) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFilePath)) {
            outputStream.write(data);
            // for testing if files keys are generated correctly
            // String base64Content = Base64.getEncoder().encodeToString(data);
            // System.out.println("Base64 Encoded File Contents of " + outputFilePath +
            // ":\n" + base64Content);
            outputStream.close();
        }
    }

    /**
     * Reads the contents of a file and returns it as a byte array.
     *
     * @param inputFilePath The path of the file to read.
     * @return A byte array containing the file's contents.
     * @throws IOException If an I/O error occurs during the file read operation.
     */
    public static byte[] readFile(String inputFilePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(inputFilePath)) {
            int size = inputStream.available();
            byte[] content = new byte[size];
            if (inputStream.read(content) == -1) {
                throw new IOException("End of file reached unexpectedly.");
            }
            inputStream.close();
            return content;
        }
    }
}
