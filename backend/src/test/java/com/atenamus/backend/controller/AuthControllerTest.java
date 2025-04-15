package com.atenamus.backend.controller;

import com.atenamus.backend.dto.SignInRequest;
import com.atenamus.backend.dto.SignUpRequest;
import com.atenamus.backend.security.JwtUtil;
import com.atenamus.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private SignUpRequest signUpRequest;
    private SignInRequest signInRequest;
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String fullName = "Test User";
    private final List<String> attributes = Arrays.asList("role:admin", "department:IT");
    private final String token = "jwt.token.string";

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        signUpRequest.setFullName(fullName);
        signUpRequest.setAttributes(attributes);

        signInRequest = new SignInRequest();
        signInRequest.setEmail(email);
        signInRequest.setPassword(password);
    }

    @Test
    void signupSuccessTest() {
        when(authService.signup(signUpRequest)).thenReturn(token);

        ResponseEntity<Map<String, String>> response = authController.signup(signUpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().get("token"));
        verify(authService).signup(signUpRequest);
    }

    @Test
    void signinSuccessTest() {
        when(authService.signin(signInRequest)).thenReturn(token);

        ResponseEntity<Map<String, String>> response = authController.signin(signInRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().get("token"));
        verify(authService).signin(signInRequest);
    }

    @Test
    void validateTokenSuccessTest() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);

        ResponseEntity<Void> response = authController.validateToken("Bearer valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil).validateToken("valid-token");
    }

    @Test
    void validateTokenInvalidTest() {
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        ResponseEntity<Void> response = authController.validateToken("Bearer invalid-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil).validateToken("invalid-token");
    }

    @Test
    void validateTokenMissingBearerTest() {
        ResponseEntity<Void> response = authController.validateToken("invalid-token-format");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void validateTokenNullHeaderTest() {
        ResponseEntity<Void> response = authController.validateToken(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void validateTokenExceptionTest() {
        when(jwtUtil.validateToken(anyString())).thenThrow(new RuntimeException("Token validation error"));

        ResponseEntity<Void> response = authController.validateToken("Bearer malformed-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil).validateToken("malformed-token");
    }
}