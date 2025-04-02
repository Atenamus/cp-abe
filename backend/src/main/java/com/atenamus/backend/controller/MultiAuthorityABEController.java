package com.atenamus.backend.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atenamus.backend.models.GlobalParameters;
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
            @RequestParam String globalParamsFileId,
            @RequestParam String authorityId) {
        try {
            String keyFileIds = abeService.authSetup(globalParamsFileId, authorityId);
            String[] ids = keyFileIds.split("\\|");
            return ResponseEntity.ok(Map.of(
                    "publicKeyFileId", ids[0],
                    "secretKeyFileId", ids[1],
                    "message", "Authority keys saved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Auth setup failed: " + e.getMessage());
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
}
