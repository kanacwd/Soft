package com.aiu.scrs.controller;

import com.aiu.scrs.config.JwtTokenProvider;
import com.aiu.scrs.dto.ApiResponse;
import com.aiu.scrs.dto.auth.AuthResponse;
import com.aiu.scrs.dto.auth.LoginRequest;
import com.aiu.scrs.dto.auth.RegisterRequest;
import com.aiu.scrs.entity.User;
import com.aiu.scrs.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - Handles login and registration
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = userService.registerUser(registerRequest);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Validate JWT token
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (tokenProvider.validateToken(token)) {
                    return ResponseEntity.ok(ApiResponse.success("Token is valid", tokenProvider.getUsernameFromToken(token)));
                }
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }

    /**
     * Get current user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = tokenProvider.getUsernameFromToken(token);
                User currentUser = userService.findByUsername(username);
                return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", currentUser));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Authorization header missing or invalid"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to get user: " + e.getMessage()));
        }
    }
}
