package com.atenamus.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.atenamus.backend.service.MultiAuthorityABEService;

@RestController
@RequestMapping("/api/mcpabe")
public class MultiAuthorityABEController {
    @Autowired
    private MultiAuthorityABEService abeService;

    @PostMapping("/global-setup")
    public ResponseEntity<?> setup(
            @RequestParam int securityParameter,
            @RequestBody SetupRequest request) {
        try {
            String fileId = abeService.globalSetup(
                    securityParameter,
                    request.getAttributeUniverse(),
                    request.getAuthorityUniverse());
            return ResponseEntity.ok(Map.of("fileId", fileId, "message", "Global parameters saved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Setup failed: " + e.getMessage());
        }
    }

    @PostMapping("/auth-setup")
    public ResponseEntity<?> authSetup(
            @RequestParam String authorityId) {
        try {
            Map<String, String> keyFileIds = abeService.authSetup(authorityId);
            return ResponseEntity.ok(Map.of(
                    "publicKeyFileId", keyFileIds.get("publicKeyFileId"),
                    "message", "Authority keys saved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Auth setup failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/key-gen", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> keyGen(
            @RequestBody KeyGenRequest request) {
        try {
            byte[] userKeyBytes = abeService.keyGen(
                    request.getAuthorityId(),
                    request.getGid(),
                    request.getAttributes());

            // Prepare the response for file download
            ByteArrayResource resource = new ByteArrayResource(userKeyBytes);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=usk_" + request.getGid() + ".dat");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(userKeyBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("KeyGen failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/encrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> encrypt(
            @RequestPart("file") MultipartFile file,
            @RequestPart("policy") String policy) {
        try {
            byte[] fileData = file.getBytes();
            byte[] ciphertextBytes = abeService.encrypt(fileData, policy);
            ByteArrayResource resource = new ByteArrayResource(ciphertextBytes);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=ciphertext_" + file.getOriginalFilename() + ".cpabe");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(ciphertextBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Encryption failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/decrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> decrypt(
            @RequestPart("ciphertext") MultipartFile ciphertextFile,
            @RequestPart("keys") List<MultipartFile> keyFiles) {
        try {
            byte[] ciphertextBytes = ciphertextFile.getBytes();
            List<byte[]> userKeyBytesList = new ArrayList<>();
            for (MultipartFile keyFile : keyFiles) {
                userKeyBytesList.add(keyFile.getBytes());
            }

            byte[] decryptedData = abeService.decrypt(ciphertextBytes, userKeyBytesList);

            // Prepare the response for file download
            ByteArrayResource resource = new ByteArrayResource(decryptedData);
            String originalFilename = ciphertextFile.getOriginalFilename().replace(".cpabe", "");
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + originalFilename);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(decryptedData.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Decryption failed: " + e.getMessage());
        }
    }

    static class SetupRequest {
        private Set<String> attributeUniverse;
        private Set<String> authorityUniverse;

        // Getters and setters
        public Set<String> getAttributeUniverse() {
            return attributeUniverse;
        }

        public void setAttributeUniverse(Set<String> attributeUniverse) {
            this.attributeUniverse = attributeUniverse;
        }

        public Set<String> getAuthorityUniverse() {
            return authorityUniverse;
        }

        public void setAuthorityUniverse(Set<String> authorityUniverse) {
            this.authorityUniverse = authorityUniverse;
        }
    }

    static class KeyGenRequest {
        private String authorityId;
        private String gid;
        private Set<String> attributes;

        public String getAuthorityId() {
            return authorityId;
        }

        public void setAuthorityId(String authorityId) {
            this.authorityId = authorityId;
        }

        public String getGid() {
            return gid;
        }

        public void setGid(String gid) {
            this.gid = gid;
        }

        public Set<String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Set<String> attributes) {
            this.attributes = attributes;
        }
    }

    public class EncryptRequest {
        private String policy; // e.g., "(attr1@auth1 AND attr2@auth1) OR attr3@auth2"

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }
    }
}
