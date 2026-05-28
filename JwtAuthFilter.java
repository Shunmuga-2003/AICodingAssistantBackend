package com.AI.CodeAssistant.config;

import com.AI.CodeAssistant.service.JwtService;
import com.AI.CodeAssistant.service
        .UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.security.core.userdetails
        .UserDetails;
import org.springframework.security.web.authentication
        .WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter
        .OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl
            userDetailsService;

    // ✅ Skip filter for public endpoints
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
                path.startsWith(
                        "/api/questions/public/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail =
                    jwtService.extractUsername(jwt);

            if (userEmail != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        userEmail);

                if (jwtService.isTokenValid(
                        jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken
                            authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails
                                            .getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalid or expired
            // Let Spring Security handle it
        }

        filterChain.doFilter(request, response);
    }
}