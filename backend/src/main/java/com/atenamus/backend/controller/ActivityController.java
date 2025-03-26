package com.atenamus.backend.controller;

import com.atenamus.backend.models.User;
import com.atenamus.backend.models.UserActivity;
import com.atenamus.backend.repository.UserRepository;
import com.atenamus.backend.security.JwtUtil;
import com.atenamus.backend.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/activity")
@CrossOrigin(origins = "*")
@Slf4j
public class ActivityController {
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final JwtUtil jwtUtil;

    public ActivityController(UserRepository userRepository, ActivityService activityService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivities(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new ResponseEntity<>("Invalid or missing token", HttpStatus.UNAUTHORIZED);
        }

        try {
            String email = jwtUtil.extractEmail(token.substring(7));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            return ResponseEntity.ok(activityService.getRecentActivities(user.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching activities: " + e.getMessage());
        }
    }
}