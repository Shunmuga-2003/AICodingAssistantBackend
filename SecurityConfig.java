package com.AI.CodeAssistant.config;

import com.AI.CodeAssistant.service
        .UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao
        .DaoAuthenticationProvider;
import org.springframework.security.config.annotation
        .authentication.configuration
        .AuthenticationConfiguration;
import org.springframework.security.config.annotation
        .web.builders.HttpSecurity;
import org.springframework.security.config.annotation
        .web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation
        .web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http
        .SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt
        .BCryptPasswordEncoder;
import org.springframework.security.crypto.password
        .PasswordEncoder;
import org.springframework.security.web
        .SecurityFilterChain;
import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl
            userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors
                        .configurationSource(
                                corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth

                        // ── Public routes ─────────────────
                        // No JWT needed for these endpoints.
                        // /api/auth/** covers login, register,
                        // health and any future auth routes.
                        // /api/questions/public/** allows
                        // unauthenticated question browsing.
                        // /error is needed for Spring's
                        // internal error handling page.
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/questions/public/**",
                                "/error"
                        ).permitAll()

                        // ── Protected routes ──────────────
                        // Every other endpoint requires a
                        // valid JWT token in the
                        // Authorization: Bearer <token> header.
                        .anyRequest().authenticated()
                )

                // ── Stateless session ─────────────────
                // No HTTP session is created or used.
                // Each request must carry its own JWT.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authenticationProvider(
                        authenticationProvider())

                // ── JWT filter ────────────────────────
                // Runs before Spring's default username/
                // password filter to extract and validate
                // the JWT from every incoming request.
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter
                                .class);

        return http.build();
    }

    // ── CORS ──────────────────────────────────────
    // Defines which origins, methods and headers
    // are allowed to call the backend.
    // Trailing slash removed from Netlify URL to
    // avoid duplicate-match issues.
    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175",
                "http://localhost:5176",
                "http://localhost:5177",
                "https://aibasedinterviewassistant.netlify.app"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        // Allow all headers (Authorization,
        // Content-Type, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Required for sending cookies /
        // Authorization headers cross-origin
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(
                "/**", config);
        return source;
    }

    // ── Authentication Provider ───────────────────
    // Wires together UserDetailsService (loads user
    // from DB) and BCrypt (verifies password).
    @Bean
    public AuthenticationProvider
    authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(
                userDetailsService);
        provider.setPasswordEncoder(
                passwordEncoder());
        return provider;
    }

    // ── Password Encoder ──────────────────────────
    // BCrypt hashes passwords before storing and
    // verifies them on login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Authentication Manager ────────────────────
    // Used by AuthController to trigger the
    // authentication process manually.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}