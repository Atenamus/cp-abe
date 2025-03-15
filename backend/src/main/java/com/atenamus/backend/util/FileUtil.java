package com.atenamus.backend.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    public static void writeCpabeFile(String encfile,
            byte[] cphBuf, byte[] aesBuf) throws IOException {
        int i;
        try (OutputStream os = new FileOutputStream(encfile)) {
            /* write aes_buf */
            for (i = 3; i >= 0; i--)
                os.write(((aesBuf.length & (0xff << 8 * i)) >> 8 * i));
            os.write(aesBuf);
            /* write cph_buf */
            for (i = 3; i >= 0; i--)
                os.write(((cphBuf.length & (0xff << 8 * i)) >> 8 * i));
            os.write(cphBuf);
            os.close();
        }

    }

    public static byte[][] readCpabeFile(String encryptedFile) throws IOException {
        int i, len;
        try (InputStream is = new FileInputStream(encryptedFile)) {
            byte[][] res = new byte[2][];
            byte[] aesBuf, cphBuf;

            /* read aes buf */
            len = 0;
            for (i = 3; i >= 0; i--) {
                int readByte = is.read();
                if (readByte == -1) {
                    throw new IOException("Unexpected end of file while reading AES buffer length.");
                }
                len |= readByte << (i * 8);
            }
            aesBuf = new byte[len];

            if (is.read(aesBuf) != len) {
                throw new IOException("Unexpected end of file while reading AES buffer.");
            }

            /* read cph buf */
            len = 0;
            for (i = 3; i >= 0; i--) {
                int readByte = is.read();
                if (readByte == -1) {
                    throw new IOException("Unexpected end of file while reading CPH buffer length.");
                }
                len |= readByte << (i * 8);
            }
            cphBuf = new byte[len];

            if (is.read(cphBuf) != len) {
                throw new IOException("Unexpected end of file while reading CPH buffer.");
            }

            is.close();

            res[0] = aesBuf;
            res[1] = cphBuf;
            return res;
        }
    }
}
