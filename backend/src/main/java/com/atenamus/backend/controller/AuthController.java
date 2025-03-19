package com.atenamus.backend.controller;

import com.atenamus.backend.dto.SignInRequest;
import com.atenamus.backend.dto.SignUpRequest;
import com.atenamus.backend.service.AuthService;
import com.atenamus.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignUpRequest request) {
        String token = authService.signup(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, String>> signin(@RequestBody SignInRequest request) {
        String token = authService.signin(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    return ResponseEntity.ok().build();
                }
            }
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}