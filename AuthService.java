package com.AI.CodeAssistant.service;

import com.AI.CodeAssistant.dto.request.LoginRequest;
import com.AI.CodeAssistant.dto.request.RegisterRequest;
import com.AI.CodeAssistant.dto.response.AuthResponse;
import com.AI.CodeAssistant.model.User;
import com.AI.CodeAssistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: "
                            + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .targetRole(request.getTargetRole())
                .targetCompany(request.getTargetCompany())
                .experienceYears(
                        request.getExperienceYears())
                .role(User.Role.USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        var userDetails = userDetailsService
                .loadUserByUsername(savedUser.getEmail());
        String token = jwtService
                .generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .targetRole(savedUser.getTargetRole())
                .targetCompany(savedUser.getTargetCompany())
                .message("Registration successful!")
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "Account is deactivated");
        }

        var userDetails = userDetailsService
                .loadUserByUsername(user.getEmail());
        String token = jwtService
                .generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .targetRole(user.getTargetRole())
                .targetCompany(user.getTargetCompany())
                .message("Login successful!")
                .build();
    }
}