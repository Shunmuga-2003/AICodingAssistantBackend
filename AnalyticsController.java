package com.AI.CodeAssistant.controller;

import com.AI.CodeAssistant.dto.response.*;
import com.AI.CodeAssistant.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation
        .AuthenticationPrincipal;
import org.springframework.security.core.userdetails
        .UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<ProgressResponse>>
    progress(
            @AuthenticationPrincipal
            UserDetails user) {

        ProgressResponse response = analyticsService
                .getProgress(user.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(response,
                        "Progress data"));
    }
}