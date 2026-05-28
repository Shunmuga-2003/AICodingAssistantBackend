package com.AI.CodeAssistant.controller;

import com.AI.CodeAssistant.dto.request.SessionRequest;
import com.AI.CodeAssistant.dto.response.*;
import com.AI.CodeAssistant.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation
        .AuthenticationPrincipal;
import org.springframework.security.core.userdetails
        .UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SessionResponse>>
    start(
            @Valid @RequestBody SessionRequest request,
            @AuthenticationPrincipal
            UserDetails user) {

        SessionResponse response = sessionService
                .startSession(
                        user.getUsername(), request);

        return ResponseEntity.ok(
                ApiResponse.success(response,
                        "Session started"));
    }

    @GetMapping("/{sessionId}/next-question")
    public ResponseEntity<ApiResponse<QuestionResponse>>
    nextQuestion(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal
            UserDetails user) {

        QuestionResponse response = sessionService
                .getNextQuestion(
                        sessionId, user.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(response,
                        "Next question"));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<SessionResponse>>
    end(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal
            UserDetails user) {

        SessionResponse response = sessionService
                .endSession(
                        sessionId, user.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(response,
                        "Session completed!"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<?>>>
    history(
            @AuthenticationPrincipal
            UserDetails user) {

        var sessions = sessionService
                .getHistory(user.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(sessions,
                        "Session history"));
    }
}