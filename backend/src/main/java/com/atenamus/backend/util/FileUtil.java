package com.atenamus.backend.util;

import java.io.ByteArrayInputStream;
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

    /**
     * Writes encrypted data to a file with the following format:
     * - 4 bytes: Length of CP-ABE encrypted structure
     * - CP-ABE encrypted structure bytes
     * - 4 bytes: Length of encrypted AES key
     * - Encrypted AES key bytes
     * - 4 bytes: Length of AES encrypted data
     * - AES encrypted data bytes
     * 
     * @param encfile         The output file path
     * @param cphKeyBuf       The CP-ABE encrypted structure
     * @param encryptedAesKey The encrypted AES key bytes
     * @param aesBuf          The AES encrypted data
     * @throws IOException If an I/O error occurs
     */
    public static void writeFullCpabeFile(String encfile, byte[] cphKeyBuf, byte[] encryptedAesKey, byte[] aesBuf)
            throws IOException {
        int i;
        try (OutputStream os = new FileOutputStream(encfile)) {
            /* write cpabe-encrypted structure length */
            for (i = 3; i >= 0; i--)
                os.write(((cphKeyBuf.length & (0xff << 8 * i)) >> 8 * i));
            /* write cpabe-encrypted structure */
            os.write(cphKeyBuf);

            /* write encrypted aes key length */
            for (i = 3; i >= 0; i--)
                os.write(((encryptedAesKey.length & (0xff << 8 * i)) >> 8 * i));
            /* write encrypted aes key */
            os.write(encryptedAesKey);

            /* write aes-encrypted data length */
            for (i = 3; i >= 0; i--)
                os.write(((aesBuf.length & (0xff << 8 * i)) >> 8 * i));
            /* write aes-encrypted data */
            os.write(aesBuf);
            os.close();
        }
    }

    /**
     * Reads encrypted data from a file with the following format:
     * - 4 bytes: Length of CP-ABE encrypted structure
     * - CP-ABE encrypted structure bytes
     * - 4 bytes: Length of encrypted AES key
     * - Encrypted AES key bytes
     * - 4 bytes: Length of AES encrypted data
     * - AES encrypted data bytes
     * 
     * @param encryptedFile The input file path
     * @return A 3D array where [0] contains AES encrypted data, [1] contains
     *         encrypted AES key, and [2] contains CP-ABE encrypted structure
     * @throws IOException If an I/O error occurs
     */
    public static byte[][] readFullCpabeFile(String encryptedFile) throws IOException {
        int i, len;
        try (InputStream is = new FileInputStream(encryptedFile)) {
            byte[][] res = new byte[3][];
            byte[] cphKeyBuf, encryptedAesKey, aesBuf;

            /* read cpabe-encrypted structure length */
            len = 0;
            for (i = 3; i >= 0; i--) {
                int readByte = is.read();
                if (readByte == -1) {
                    throw new IOException("Unexpected end of file while reading CP-ABE structure buffer length.");
                }
                len |= readByte << (i * 8);
            }
            cphKeyBuf = new byte[len];

            /* read cpabe-encrypted structure */
            if (is.read(cphKeyBuf) != len) {
                throw new IOException("Unexpected end of file while reading CP-ABE structure buffer.");
            }

            /* read encrypted aes key length */
            len = 0;
            for (i = 3; i >= 0; i--) {
                int readByte = is.read();
                if (readByte == -1) {
                    throw new IOException("Unexpected end of file while reading encrypted AES key buffer length.");
                }
                len |= readByte << (i * 8);
            }
            encryptedAesKey = new byte[len];

            /* read encrypted aes key */
            if (is.read(encryptedAesKey) != len) {
                throw new IOException("Unexpected end of file while reading encrypted AES key buffer.");
            }

            /* read aes-encrypted data length */
            len = 0;
            for (i = 3; i >= 0; i--) {
                int readByte = is.read();
                if (readByte == -1) {
                    throw new IOException("Unexpected end of file while reading AES buffer length.");
                }
                len |= readByte << (i * 8);
            }
            aesBuf = new byte[len];

            /* read aes-encrypted data */
            if (is.read(aesBuf) != len) {
                throw new IOException("Unexpected end of file while reading AES buffer.");
            }

            is.close();

            // Return AES encrypted data first, then encrypted AES key, then CP-ABE
            // encrypted structure
            res[0] = aesBuf;
            res[1] = encryptedAesKey;
            res[2] = cphKeyBuf;
            return res;
        }
    }

    public static byte[][] readFullCpabeFile(byte[] encryptedData) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(encryptedData);
        int i, len;
        byte[][] res = new byte[3][];
        byte[] cphKeyBuf, encryptedAesKey, aesBuf;

        /* Read CP-ABE encrypted structure length */
        len = 0;
        for (i = 3; i >= 0; i--) {
            int readByte = is.read();
            if (readByte == -1) {
                throw new IOException("Unexpected end of file while reading CP-ABE structure buffer length.");
            }
            len |= readByte << (i * 8);
        }
        cphKeyBuf = new byte[len];

        /* Read CP-ABE encrypted structure */
        if (is.read(cphKeyBuf) != len) {
            throw new IOException("Unexpected end of file while reading CP-ABE structure buffer.");
        }

        /* Read encrypted AES key length */
        len = 0;
        for (i = 3; i >= 0; i--) {
            int readByte = is.read();
            if (readByte == -1) {
                throw new IOException("Unexpected end of file while reading encrypted AES key buffer length.");
            }
            len |= readByte << (i * 8);
        }
        encryptedAesKey = new byte[len];

        /* Read encrypted AES key */
        if (is.read(encryptedAesKey) != len) {
            throw new IOException("Unexpected end of file while reading encrypted AES key buffer.");
        }

        /* Read AES-encrypted data length */
        len = 0;
        for (i = 3; i >= 0; i--) {
            int readByte = is.read();
            if (readByte == -1) {
                throw new IOException("Unexpected end of file while reading AES buffer length.");
            }
            len |= readByte << (i * 8);
        }
        aesBuf = new byte[len];

        /* Read AES-encrypted data */
        if (is.read(aesBuf) != len) {
            throw new IOException("Unexpected end of file while reading AES buffer.");
        }

        is.close();

        res[0] = aesBuf;
        res[1] = encryptedAesKey;
        res[2] = cphKeyBuf;
        return res;
    }

}
