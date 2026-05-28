package com.AI.CodeAssistant.controller;

import com.AI.CodeAssistant.dto.request.AnswerRequest;
import com.AI.CodeAssistant.dto.response.*;
import com.AI.CodeAssistant.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation
        .AuthenticationPrincipal;
import org.springframework.security.core.userdetails
        .UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/{sessionId}/submit/text")
    public ResponseEntity<ApiResponse<FeedbackResponse>>
    submitText(
            @PathVariable Long sessionId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal
            UserDetails user) {

        FeedbackResponse feedback = answerService
                .submitTextAnswer(
                        sessionId, request,
                        user.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(feedback,
                        "Answer evaluated ✅"));
    }

    @PostMapping("/{sessionId}/submit/voice")
    public ResponseEntity<ApiResponse<FeedbackResponse>>
    submitVoice(
            @PathVariable Long sessionId,
            @RequestParam("audio")
            MultipartFile audio,
            @RequestParam("questionId")
            Long questionId,
            @RequestParam(value = "duration",
                    defaultValue = "0")
            Integer duration,
            @AuthenticationPrincipal
            UserDetails user) {

        FeedbackResponse feedback = answerService
                .submitVoiceAnswer(
                        sessionId, questionId,
                        audio, duration,
                        user.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(feedback,
                        "Voice evaluated ✅"));
    }

    @GetMapping("/{sessionId}/all")
    public ResponseEntity<ApiResponse<?>> getAnswers(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails user) {

        var answers = answerService
                .getSessionAnswers(sessionId);

        return ResponseEntity.ok(
                ApiResponse.success(answers,
                        "Session answers"));
    }
}