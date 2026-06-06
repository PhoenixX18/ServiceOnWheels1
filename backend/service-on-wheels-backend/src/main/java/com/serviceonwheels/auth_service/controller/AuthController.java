package com.serviceonwheels.auth_service.controller;

import com.serviceonwheels.auth_service.dto.AuthResponse;
import com.serviceonwheels.auth_service.dto.ForgotPasswordRequest;
import com.serviceonwheels.auth_service.dto.LoginRequest;
import com.serviceonwheels.auth_service.dto.RegisterRequest;
import com.serviceonwheels.auth_service.dto.ResetPasswordRequest;
import com.serviceonwheels.auth_service.service.AuthService;
import com.serviceonwheels.auth_service.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email: {}", request.getEmail());
        AuthResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.getEmail());
        AuthResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/auth/forgot-password - email: {}", request.getEmail());
        Map<String, String> response = passwordResetService.handleForgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/auth/reset-password");
        Map<String, String> response = passwordResetService.handleResetPassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, String>> validateResetToken(@RequestParam String token) {
        log.info("GET /api/auth/validate-reset-token");
        Map<String, String> response = passwordResetService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}

