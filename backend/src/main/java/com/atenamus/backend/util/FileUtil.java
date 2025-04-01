package com.atenamus.backend.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {

    // Common MIME types mapping
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // Documents
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("rtf", "application/rtf");

        // Images
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("svg", "image/svg+xml");

        // Archives
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("rar", "application/x-rar-compressed");
        MIME_TYPES.put("7z", "application/x-7z-compressed");
        MIME_TYPES.put("tar", "application/x-tar");

        // Other
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("csv", "text/csv");
        MIME_TYPES.put("json", "application/json");
    }

    /**
     * Determines the MIME type based on a file extension
     * 
     * @param filename The filename including extension
     * @return The MIME type or application/octet-stream if unknown
     */
    public static String getMimeType(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String extension = filename.substring(lastDotIndex + 1).toLowerCase();
            return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
        }
        return "application/octet-stream";
    }

    /**
     * Detects the original file type from a filename and formats the decrypted
     * filename
     * 
     * @param encryptedFilename The name of the encrypted file
     * @return The decrypted filename without .cpabe extension
     */
    public static String getDecryptedFilename(String encryptedFilename) {
        if (encryptedFilename == null || encryptedFilename.isEmpty()) {
            return "decrypted-file";
        }

        if (encryptedFilename.toLowerCase().endsWith(".cpabe")) {
            return encryptedFilename.substring(0, encryptedFilename.length() - 6);
        }

        return encryptedFilename;
    }

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
     * - Original file extension (for decryption type identification)
     * 
     * @param encfile          The output file path
     * @param cphKeyBuf        The CP-ABE encrypted structure
     * @param encryptedAesKey  The encrypted AES key bytes
     * @param aesBuf           The AES encrypted data
     * @param originalFileType The original file's extension/type
     * @throws IOException If an I/O error occurs
     */
    public static void writeFullCpabeFile(String encfile, byte[] cphKeyBuf, byte[] encryptedAesKey, byte[] aesBuf,
            String originalFileType) throws IOException {
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

            /* Write original file type if provided */
            if (originalFileType != null && !originalFileType.isEmpty()) {
                byte[] fileTypeBytes = originalFileType.getBytes();
                for (i = 3; i >= 0; i--)
                    os.write(((fileTypeBytes.length & (0xff << 8 * i)) >> 8 * i));
                os.write(fileTypeBytes);
            } else {
                // Write zero length if no file type
                for (i = 3; i >= 0; i--)
                    os.write(0);
            }

            os.close();
        }
    }

    /**
     * Overload of writeFullCpabeFile that doesn't specify file type
     * (For backward compatibility)
     */
    public static void writeFullCpabeFile(String encfile, byte[] cphKeyBuf, byte[] encryptedAesKey, byte[] aesBuf)
            throws IOException {
        writeFullCpabeFile(encfile, cphKeyBuf, encryptedAesKey, aesBuf, null);
    }

    /**
     * Reads encrypted data from a file with the following format:
     * - 4 bytes: Length of CP-ABE encrypted structure
     * - CP-ABE encrypted structure bytes
     * - 4 bytes: Length of encrypted AES key
     * - Encrypted AES key bytes
     * - 4 bytes: Length of AES encrypted data
     * - AES encrypted data bytes
     * - Original file extension (if available)
     * 
     * @param encryptedFile The input file path
     * @return A 4D array where [0] contains AES encrypted data, [1] contains
     *         encrypted AES key, [2] contains CP-ABE encrypted structure, and
     *         [3] contains original file type bytes (may be empty)
     * @throws IOException If an I/O error occurs
     */
    public static byte[][] readFullCpabeFile(String encryptedFile) throws IOException {
        int i, len;
        try (InputStream is = new FileInputStream(encryptedFile)) {
            byte[][] res = new byte[4][]; // Added fourth element for file type
            byte[] cphKeyBuf, encryptedAesKey, aesBuf, fileTypeBuf = null;

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

            /* Try to read file type information if available */
            try {
                /* read file type length */
                len = 0;
                for (i = 3; i >= 0; i--) {
                    int readByte = is.read();
                    if (readByte == -1) {
                        // End of file, older format without file type
                        len = 0;
                        break;
                    }
                    len |= readByte << (i * 8);
                }

                if (len > 0) {
                    fileTypeBuf = new byte[len];
                    is.read(fileTypeBuf); // Read file type bytes
                } else {
                    fileTypeBuf = new byte[0];
                }
            } catch (IOException e) {
                // Older format file, ignore and use empty file type
                fileTypeBuf = new byte[0];
            }

            is.close();

            // Return data
            res[0] = aesBuf;
            res[1] = encryptedAesKey;
            res[2] = cphKeyBuf;
            res[3] = fileTypeBuf; // Original file type or empty
            return res;
        }
    }

    public static byte[][] readFullCpabeFile(byte[] encryptedData) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(encryptedData);
        int i, len;
        byte[][] res = new byte[4][];
        byte[] cphKeyBuf, encryptedAesKey, aesBuf, fileTypeBuf = null;

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

        /* Try to read file type information if available */
        try {
            /* read file type length */
            len = 0;
            for (i = 3; i >= 0; i--) {
                int readByte = is.read();
                if (readByte == -1) {
                    // End of file, older format without file type
                    len = 0;
                    break;
                }
                len |= readByte << (i * 8);
            }

            if (len > 0) {
                fileTypeBuf = new byte[len];
                is.read(fileTypeBuf); // Read file type bytes
            } else {
                fileTypeBuf = new byte[0];
            }
        } catch (IOException e) {
            // Older format file, ignore and use empty file type
            fileTypeBuf = new byte[0];
        }

        is.close();

        res[0] = aesBuf;
        res[1] = encryptedAesKey;
        res[2] = cphKeyBuf;
        res[3] = fileTypeBuf; // Original file type or empty
        return res;
    }
}
