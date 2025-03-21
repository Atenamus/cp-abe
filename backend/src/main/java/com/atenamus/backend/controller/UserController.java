package com.atenamus.backend.controller;

import com.atenamus.backend.dto.CreatePolicy;
import com.atenamus.backend.models.User;
import com.atenamus.backend.models.UserPolicy;
import com.atenamus.backend.repository.UserRepository;
import com.atenamus.backend.security.JwtUtil;
import com.atenamus.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, UserService userService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/create-policy")
    public ResponseEntity<?> createPolicy(@RequestBody CreatePolicy request,
                                          HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            log.error("Invalid or missing token");
            return new ResponseEntity<>("Invalid or missing token", HttpStatus.UNAUTHORIZED);
        }

        log.debug("Create policy token: {}", token);
        token = token.substring(7);

        try {
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            return userService.createPolicy(request, user);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/get-policy")
    public ResponseEntity<?> getPolicy(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            log.error("Invalid or missing token");
            return new ResponseEntity<>("Invalid or missing token", HttpStatus.UNAUTHORIZED);
        }

        log.debug("Get policy token: {}", token);
        token = token.substring(7);

        try {
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            ResponseEntity<?> policy  = userService.getPolicy(user);
            return new ResponseEntity<>(policy, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
