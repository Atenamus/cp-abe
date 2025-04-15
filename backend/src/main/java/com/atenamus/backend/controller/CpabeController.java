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
import org.springframework.web.bind.annotation.RequestHeader;
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
import com.atenamus.backend.models.User;
import com.atenamus.backend.repository.UserRepository;
import com.atenamus.backend.security.JwtUtil;
import com.atenamus.backend.service.ActivityService;
import com.atenamus.backend.service.KeyInitializationService;
import com.atenamus.backend.util.AESCoder;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;
import it.unisa.dia.gas.jpbc.Element;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cpabe")
@CrossOrigin(origins = "*")
public class CpabeController {
    private static final String ENCRYPTED_FILES_DIR = "encrypted_files";
    private final Path projectRoot;
    private final Path encryptedFilesPath;

    @Autowired
    private Cpabe cpabe;

    @Autowired
    private KeyInitializationService keyInitService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserRepository userRepository;

    public CpabeController() {
        this.projectRoot = Paths.get("").toAbsolutePath();
        this.encryptedFilesPath = projectRoot.resolve(ENCRYPTED_FILES_DIR);
    }

    private Path getUserSpecificPath(String userId) {
        return encryptedFilesPath.resolve(userId);
    }

    @GetMapping("/setup")
    public Map<String, Object> setup() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if keys already exist
            boolean pubKeyExists = Files.exists(keyInitService.getPublicKeyPath());
            boolean mskExists = Files.exists(keyInitService.getMasterKeyPath());

            // Initialize keys if they don't exist
            if (!pubKeyExists || !mskExists) {
                try {
                    keyInitService.initializeKeys();
                } catch (Exception e) {
                    response.put("error", "Failed to initialize keys: " + e.getMessage());
                    return response;
                }
            }

            // Read the existing keys for the response
            byte[] pubBytes = Files.readAllBytes(keyInitService.getPublicKeyPath());
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            byte[] mskBytes = Files.readAllBytes(keyInitService.getMasterKeyPath());
            MasterSecretKey msk = SerializeUtil.unserializeMasterSecretKey(pub, mskBytes);

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
            response.put("message", "Setup complete! Public and master keys are ready.");

        } catch (IOException e) {
            response.put("error", "An error occurred during CP-ABE setup: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/keygen")
    public ResponseEntity<byte[]> keygen(@RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {
        File tempFile = null;
        try {
            // Validate input
            @SuppressWarnings("unchecked")
            List<String> attributesList = (List<String>) request.get("attributes");
            if (attributesList == null || attributesList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Attributes list cannot be empty".getBytes());
            }

            String[] attributes = attributesList.toArray(new String[0]);
            // Validate each attribute
            for (String attr : attributes) {
                if (attr == null || attr.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("Invalid attribute found".getBytes());
                }
            }

            // Check for keys and generate if missing
            boolean pubKeyExists = Files.exists(keyInitService.getPublicKeyPath());
            boolean mskExists = Files.exists(keyInitService.getMasterKeyPath());

            if (!pubKeyExists || !mskExists) {
                try {
                    keyInitService.initializeKeys();
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(("Failed to initialize keys: " + e.getMessage()).getBytes());
                }
            }

            System.out.println("Generating keys for attributes: " + Arrays.toString(attributes));

            byte[] pubBytes = Files.readAllBytes(keyInitService.getPublicKeyPath());
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            byte[] mskBytes = Files.readAllBytes(keyInitService.getMasterKeyPath());
            MasterSecretKey msk = SerializeUtil.unserializeMasterSecretKey(pub, mskBytes);

            PrivateKey prv = cpabe.keygen(pub, msk, attributes);

            // Add traceability information
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            prv.userId = user.getId().toString();
            prv.userEmail = user.getEmail();
            prv.timestamp = System.currentTimeMillis();

            // Set expiration to 1 year from now
            prv.expirationDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000);

            byte[] prvBytes = SerializeUtil.serializePrivateKey(prv);

            // Write to temp file with better error handling
            tempFile = File.createTempFile("private_key", ".dat");
            FileUtil.writeFile(tempFile.getAbsolutePath(), prvBytes);
            byte[] privateKeyBytes = Files.readAllBytes(tempFile.toPath());

            String privateKeyName = "private_key.dat";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", privateKeyName);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");

            // Track key generation activity
            activityService.trackActivity(user.getId(), "key_generated", "Private Key",
                    "Generated private key with attributes: " + String.join(", ", attributes));

            System.out.println("Private key generated successfully: " + privateKeyName);
            return new ResponseEntity<>(privateKeyBytes, headers, HttpStatus.OK);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Key Generation failed: " + e.getMessage()).getBytes());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @PostMapping("/encrypt")
    public ResponseEntity<byte[]> encrypt(@RequestParam("file") MultipartFile file,
            @RequestParam("policy") String policy,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String userId = extractUserIdFromToken(authHeader.replace("Bearer ", ""));
            Path userPath = getUserSpecificPath(userId);
            Files.createDirectories(userPath);

            String originalFilename = file.getOriginalFilename();
            byte[] plaintext = file.getBytes();

            String originalFileType = null;
            if (originalFilename != null && originalFilename.contains(".")) {
                originalFileType =
                        originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            }

            byte[] pubBytes = Files.readAllBytes(keyInitService.getPublicKeyPath());
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            CipherKey cipherKey = cpabe.encrypt(pub, policy);
            Cipher cph = cipherKey.cph;

            // Set encryption date to current time
            cph.encryptionDate = System.currentTimeMillis();

            if (cph == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create cipher for encryption".getBytes());
            }

            Element symmetric_key = cipherKey.key;
            byte[] symmetricKeyBytes = symmetric_key.toBytes();
            byte[] storedKeyBytes = symmetricKeyBytes;
            byte[] cphBuf = SerializeUtil.serializeCipher(cph);
            byte[] encryptedData = AESCoder.encrypt(symmetricKeyBytes, plaintext);

            Path encryptedFilePath = userPath.resolve(originalFilename + ".cpabe");
            FileUtil.writeFullCpabeFile(encryptedFilePath.toString(), cphBuf, storedKeyBytes,
                    encryptedData, originalFileType);
            byte[] fullEncryptedFile = Files.readAllBytes(encryptedFilePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", originalFilename + ".cpabe");

            // Track activity
            activityService.trackActivity(user.getId(), "file_encrypted", originalFilename,
                    "File encrypted with policy: " + policy);

            return new ResponseEntity<>(fullEncryptedFile, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Encryption failed: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(@RequestParam("file") MultipartFile encryptedFile,
            @RequestParam("key") MultipartFile privateKeyFile,
            @RequestHeader("Authorization") String authHeader) {
        Path tempEncryptedFile = null;
        Path tempKeyFile = null;
        try {
            String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            byte[] pubBytes = Files.readAllBytes(keyInitService.getPublicKeyPath());
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            // Create temporary files in system temp directory
            tempEncryptedFile = Files.createTempFile("encrypted_", ".cpabe");
            encryptedFile.transferTo(tempEncryptedFile.toFile());

            byte[][] encryptedData = FileUtil.readFullCpabeFile(tempEncryptedFile.toString());
            System.out.println("Encrypted data sizes: AES=" + encryptedData[0].length + ", Key="
                    + encryptedData[1].length + ", CPH=" + encryptedData[2].length);

            byte[] encryptedDataBuf = encryptedData[0];
            byte[] storedKeyBytes = encryptedData[1];
            byte[] cphBuf = encryptedData[2];
            String originalFileType = null;
            if (encryptedData[3] != null && encryptedData[3].length > 0) {
                originalFileType = new String(encryptedData[3]);
                System.out.println("Original file type: " + originalFileType);
            }

            Cipher cipher = SerializeUtil.unserializeCipher(pub, cphBuf);

            // Process private key
            tempKeyFile = Files.createTempFile("private_key_", ".dat");
            privateKeyFile.transferTo(tempKeyFile.toFile());
            PrivateKey prv =
                    SerializeUtil.unserializePrivateKey(pub, Files.readAllBytes(tempKeyFile));

            ElementBoolean result = cpabe.decrypt(pub, prv, cipher);

            if (result.satisfy) {
                try {
                    // Get decrypted AES key from CP-ABE result
                    byte[] aesKey = result.key.toBytes();
                    System.out.println("Decrypted AES key length: " + aesKey.length);

                    // Process key if needed (ensure it's 32 bytes for AES-256)
                    if (aesKey.length > 32) {
                        byte[] truncatedKey = new byte[32];
                        System.arraycopy(aesKey, 0, truncatedKey, 0, 32);
                        aesKey = truncatedKey;
                    } else if (aesKey.length < 32) {
                        byte[] paddedKey = new byte[32];
                        System.arraycopy(aesKey, 0, paddedKey, 0, aesKey.length);
                        aesKey = paddedKey;
                    }

                    // Decrypt the file content using AES
                    byte[] plaintext = AESCoder.decrypt(aesKey, encryptedDataBuf);
                    System.out.println("Decrypted plaintext length: " + plaintext.length);

                    if (plaintext.length == 0) {
                        throw new RuntimeException("Decrypted file is empty");
                    }

                    HttpHeaders headers = new HttpHeaders();
                    String outputFilename =
                            FileUtil.getDecryptedFilename(encryptedFile.getOriginalFilename());

                    if (originalFileType != null && !originalFileType.isEmpty()) {
                        if (!outputFilename.toLowerCase()
                                .endsWith("." + originalFileType.toLowerCase())) {
                            outputFilename = outputFilename + "." + originalFileType;
                        }
                        headers.setContentType(
                                MediaType.parseMediaType(FileUtil.getMimeType(outputFilename)));
                    } else {
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    }

                    headers.setContentDispositionFormData("attachment", outputFilename);

                    // Track successful decryption
                    activityService.trackActivity(user.getId(), "file_decrypted",
                            encryptedFile.getOriginalFilename(), "File decrypted successfully");

                    return new ResponseEntity<>(plaintext, headers, HttpStatus.OK);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Decryption failed: " + e.getMessage());
                }
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Your attributes do not satisfy the access policy");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Decryption failed: " + e.getMessage());
        } finally {
            // Clean up temporary files
            try {
                if (tempEncryptedFile != null) {
                    Files.deleteIfExists(tempEncryptedFile);
                }
                if (tempKeyFile != null) {
                    Files.deleteIfExists(tempKeyFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lists all encrypted files for the authenticated user
     */
    @GetMapping("/files")
    public ResponseEntity<?> listUserFiles(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user ID from auth token
            String userId = extractUserIdFromToken(authHeader.replace("Bearer ", ""));
            Path userPath = getUserSpecificPath(userId);

            // Create user directory if it doesn't exist
            if (!Files.exists(userPath)) {
                Files.createDirectories(userPath);
                return ResponseEntity.ok(new ArrayList<>());
            }

            // List files in the user's directory
            List<Map<String, Object>> files = Files.list(userPath)
                    .filter(path -> !Files.isDirectory(path) && path.toString().endsWith(".cpabe"))
                    .map(path -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        File file = path.toFile();

                        // Extract original filename (remove .cpabe extension)
                        String fileName = path.getFileName().toString();
                        String originalName = fileName;
                        if (fileName.endsWith(".cpabe")) {
                            originalName = fileName.substring(0, fileName.length() - 6);
                        }

                        fileInfo.put("id", path.getFileName().toString().hashCode());
                        fileInfo.put("name", originalName);
                        fileInfo.put("fullName", path.getFileName().toString());
                        fileInfo.put("path", path.toString());
                        fileInfo.put("size", file.length());

                        // Format date as ISO string
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        fileInfo.put("createdAt", sdf.format(file.lastModified()));

                        return fileInfo;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve files: " + e.getMessage());
        }
    }

    /**
     * Downloads a specific encrypted file
     */
    @GetMapping("/files/download")
    public ResponseEntity<?> downloadEncryptedFile(@RequestParam("filename") String filename,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader.replace("Bearer ", ""));
            Path userPath = getUserSpecificPath(userId);
            Path filePath = userPath.resolve(filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            byte[] fileBytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download file: " + e.getMessage());
        }
    }

    @PostMapping("/validate-key")
    public ResponseEntity<?> validateKey(@RequestParam("key") MultipartFile keyFile) {
        try {
            byte[] pubBytes = Files.readAllBytes(keyInitService.getPublicKeyPath());
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            // Create a temporary file for the private key
            Path tempKeyFile = Files.createTempFile("private_key_", ".dat");
            keyFile.transferTo(tempKeyFile.toFile());

            try {
                PrivateKey prv =
                        SerializeUtil.unserializePrivateKey(pub, Files.readAllBytes(tempKeyFile));

                // Validate expiration
                boolean isExpired = prv.expirationDate < System.currentTimeMillis();

                Map<String, Object> response = new HashMap<>();
                response.put("valid", !isExpired);
                response.put("attributes",
                        prv.comps.stream().map(comp -> comp.attr).collect(Collectors.toList()));
                response.put("issuedTo", prv.userEmail);
                response.put("issuedOn", new Date(prv.timestamp));
                response.put("expiresOn", new Date(prv.expirationDate));

                if (isExpired) {
                    response.put("message", "Key has expired");
                }

                return ResponseEntity.ok(response);
            } finally {
                // Cleanup temp file
                Files.deleteIfExists(tempKeyFile);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "Invalid key file: " + e.getMessage()));
        }
    }

    private String extractUserIdFromToken(String token) {
        try {
            // Extract email from token using the application's JwtUtil
            String email = jwtUtil.extractEmail(token);

            if (email == null || email.isEmpty()) {
                // Fallback to default user if token is invalid
                return "user123";
            }

            // In this implementation, we use the email as the user identifier
            return email.replaceAll("[^a-zA-Z0-9]", "_");
        } catch (Exception e) {
            // If token parsing fails, log the error and return a default user
            System.err.println("Error extracting user ID from token: " + e.getMessage());
            return "user123";
        }
    }
}
