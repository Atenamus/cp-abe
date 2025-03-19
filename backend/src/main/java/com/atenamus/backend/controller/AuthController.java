package com.atenamus.backend.controller;

import com.atenamus.backend.dto.SignInRequest;
import com.atenamus.backend.dto.SignUpRequest;
import com.atenamus.backend.service.AuthService;
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
}