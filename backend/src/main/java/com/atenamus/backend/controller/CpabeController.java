package com.atenamus.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.dto.MasterSecretKeyDto;
import com.atenamus.backend.dto.PublicKeyDto;
import com.atenamus.backend.models.Cipher;
import com.atenamus.backend.models.CipherKey;
import com.atenamus.backend.models.ElementBoolean;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.util.AESCoder;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;
import it.unisa.dia.gas.jpbc.Element;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/cpabe")
@CrossOrigin(origins = "*")
public class CpabeController {

    private static final String PUB_KEY_FILE = "public_key.dat";
    private static final String MSK_KEY_FILE = "master_secret_key.dat";
    private static final String PRV_KEY_FILE = "private_key.dat";

    @Autowired
    private Cpabe cpabe;

    @GetMapping("/setup")
    public Map<String, Object> setup() {
        Map<String, Object> response = new HashMap<>();

        try {
            PublicKey pub = new PublicKey();
            MasterSecretKey msk = new MasterSecretKey();

            cpabe.setup(pub, msk);

            byte[] pubBytes = SerializeUtil.serializePublicKey(pub);
            FileUtil.writeFile(PUB_KEY_FILE, pubBytes);

            byte[] mskBytes = SerializeUtil.serializeMasterSecretKey(msk);
            FileUtil.writeFile(MSK_KEY_FILE, mskBytes);

            PublicKeyDto pubDto = new PublicKeyDto();
            pubDto.g = pub.g.toString();
            pubDto.h = pub.h.toString();
            pubDto.f = pub.f.toString();
            pubDto.gp = pub.gp.toString();
            pubDto.g_hat_alpha = pub.g_hat_alpha.toString();

            MasterSecretKeyDto mskDto = new MasterSecretKeyDto();
            mskDto.beta = msk.beta.toString();
            mskDto.g_alpha = msk.g_alpha.toString();

            response.put("publicKey", pubDto);
            response.put("masterSecretKey", mskDto);
            response.put("message", "Setup complete! Public and master keys generated.");

        } catch (IOException e) {
            response.put("error", "An error occurred during CP-ABE setup. Please try again.");
        }

        return response;
    }

    @PostMapping("/keygen")
    public Map<String, Object> keygen(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            String[] attributes = ((List<String>) request.get("attributes")).toArray(new String[0]);

            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            byte[] mskBytes = FileUtil.readFile(MSK_KEY_FILE);
            MasterSecretKey msk = SerializeUtil.unserializeMasterSecretKey(pub, mskBytes);

            PrivateKey prv = cpabe.keygen(pub, msk, attributes);

            byte[] prvBytes = SerializeUtil.serializePrivateKey(prv);
            FileUtil.writeFile(PRV_KEY_FILE, prvBytes);

            response.put("message", "Private key generated successfully.");
            response.put("attributes", attributes);
        } catch (IOException | NoSuchAlgorithmException e) {
            response.put("error",
                    "An error occurred during private key generation: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<byte[]> encrypt(@RequestParam("file") MultipartFile file,
            @RequestParam("policy") String policy) {

        Map<String, Object> response = new HashMap<>();
        try {
            String originalFilename = file.getOriginalFilename();
            byte[] plaintext = file.getBytes(); // Read file contents

            // Extract original file extension/type
            String originalFileType = null;
            if (originalFilename != null && originalFilename.contains(".")) {
                originalFileType = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            }

            System.out.println("Encrypting file: " + originalFilename + ", type: " +
                    (originalFileType != null ? originalFileType : "unknown"));

            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            CipherKey cipherKey = cpabe.encrypt(pub, policy);
            Cipher cph = cipherKey.cph;

            if (cph == null) {
                response.put("error", "An error occurred during encryption");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create cipher for encryption".getBytes());
            }

            Element symmetric_key = cipherKey.key;
            byte[] symmetricKeyBytes = symmetric_key.toBytes();

            // Store the symmetric key bytes - this is what we'll retrieve during decryption
            byte[] storedKeyBytes = symmetricKeyBytes;
            byte[] cphBuf = SerializeUtil.serializeCipher(cph);
            byte[] encryptedData = AESCoder.encrypt(symmetricKeyBytes, plaintext);

            // Store the CP-ABE ciphertext, symmetric key bytes, and AES-encrypted data
            String encryptedFileName = originalFilename + ".cpabe";

            // Write the file to storage with file type information
            FileUtil.writeFullCpabeFile(encryptedFileName, cphBuf, storedKeyBytes, encryptedData, originalFileType);

            // Create a complete encrypted file to return in the response
            File tempFile = File.createTempFile("encrypted-", ".cpabe");
            FileUtil.writeFullCpabeFile(tempFile.getAbsolutePath(), cphBuf, storedKeyBytes, encryptedData,
                    originalFileType);
            byte[] fullEncryptedFile = Files.readAllBytes(tempFile.toPath());
            tempFile.delete(); // Clean up

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", encryptedFileName);

            return new ResponseEntity<>(fullEncryptedFile, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Encryption failed: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(@RequestParam("file") MultipartFile encryptedFile,
            @RequestParam("key") MultipartFile privateKeyFile) {
        try {
            System.out.println("Starting decryption process...");
            System.out.println("Received encrypted file: " + encryptedFile.getOriginalFilename() + ", size: "
                    + encryptedFile.getSize());

            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            // First, save the uploaded file to disk to ensure it's complete
            File tempEncryptedFile = File.createTempFile("encrypted-", ".cpabe");
            encryptedFile.transferTo(tempEncryptedFile);
            System.out.println("Saved encrypted file to: " + tempEncryptedFile.getAbsolutePath() + ", size: "
                    + tempEncryptedFile.length());

            // Use the file path instead of bytes
            byte[][] encryptedData;
            try {
                encryptedData = FileUtil.readFullCpabeFile(tempEncryptedFile.getAbsolutePath());
                System.out.println("Successfully read encrypted file structure");
            } catch (IOException e) {
                System.err.println("Error reading encrypted file: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or corrupted encrypted file format: " + e.getMessage());
            }

            byte[] encryptedDataBuf = encryptedData[0]; // AES encrypted data
            byte[] storedKeyBytes = encryptedData[1]; // Stored symmetric key bytes
            byte[] cphBuf = encryptedData[2]; // CP-ABE ciphertext

            // Check if file type information is available (index 3)
            String originalFileType = null;
            if (encryptedData.length > 3 && encryptedData[3] != null && encryptedData[3].length > 0) {
                originalFileType = new String(encryptedData[3]);
                System.out.println("Retrieved original file type: " + originalFileType);
            }

            System.out.println("AES encrypted data length: " + encryptedDataBuf.length);
            System.out.println("Stored key bytes length: " + storedKeyBytes.length);
            System.out.println("CP-ABE ciphertext length: " + cphBuf.length);

            Cipher cipher = SerializeUtil.unserializeCipher(pub, cphBuf);

            // Save and read the private key file to ensure it's complete
            File tempKeyFile = File.createTempFile("private-key-", ".dat");
            privateKeyFile.transferTo(tempKeyFile);

            PrivateKey prv = SerializeUtil.unserializePrivateKey(pub, Files.readAllBytes(tempKeyFile.toPath()));
            tempKeyFile.delete(); // Clean up

            // Verify policy satisfaction with CP-ABE
            ElementBoolean result = cpabe.decrypt(pub, prv, cipher);
            System.out.println("CP-ABE decryption result: " + result.satisfy);

            // Clean up temporary file
            tempEncryptedFile.delete();

            if (result.satisfy) {
                // Use the originally stored symmetric key bytes for decryption
                try {
                    byte[] plaintext = AESCoder.decrypt(storedKeyBytes, encryptedDataBuf);
                    System.out.println("Successfully decrypted plaintext, length: " + plaintext.length);

                    // Prepare file response
                    HttpHeaders headers = new HttpHeaders();

                    // Determine output filename and content type
                    String outputFilename = FileUtil.getDecryptedFilename(encryptedFile.getOriginalFilename());

                    // If we have original file type information, use it
                    if (originalFileType != null && !originalFileType.isEmpty()) {
                        // If the filename doesn't already have the extension, add it
                        if (!outputFilename.toLowerCase().endsWith("." + originalFileType.toLowerCase())) {
                            outputFilename = outputFilename + "." + originalFileType;
                        }

                        // Set content type based on file type
                        headers.setContentType(MediaType.parseMediaType(
                                FileUtil.getMimeType(outputFilename)));
                    } else {
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    }

                    headers.setContentDispositionFormData("attachment", outputFilename);
                    return new ResponseEntity<>(plaintext, headers, HttpStatus.OK);
                } catch (Exception e) {
                    System.err.println("AES decryption failed: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("AES decryption failed: " + e.getMessage());
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Policy not satisfied for decryption");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during decryption: " + e.getMessage());
        }
    }
}
