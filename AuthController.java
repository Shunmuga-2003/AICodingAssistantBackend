package com.AI.CodeAssistant.controller;

import com.AI.CodeAssistant.dto.request.LoginRequest;
import com.AI.CodeAssistant.dto.request.RegisterRequest;
import com.AI.CodeAssistant.dto.response.ApiResponse;
import com.AI.CodeAssistant.dto.response.AuthResponse;
import com.AI.CodeAssistant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>>
    register(
            @Valid @RequestBody
            RegisterRequest request) {

        AuthResponse response =
                authService.register(request);

        return ResponseEntity.ok(
                ApiResponse.success(response,
                        "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>>
    login(
            @Valid @RequestBody
            LoginRequest request) {

        AuthResponse response =
                authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response,
                        "Login successful"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>>
    health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Server is running!", "OK"));
    }
}