package com.AI.CodeAssistant.controller;

import com.AI.CodeAssistant.dto.response.ApiResponse;
import com.AI.CodeAssistant.model.Question;
import com.AI.CodeAssistant.model.Question.Category;
import com.AI.CodeAssistant.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<List<Question>>>
    getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService.getAll(),
                        "All questions"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Question>>>
    getByCategory(
            @PathVariable Category category) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService
                                .getByCategory(category),
                        "Questions by category"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Question>>>
    search(@RequestParam String keyword) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService.search(keyword),
                        "Search results"));
    }

    // ✅ NEW: count active questions per category
    // GET /api/questions/count?category=JAVA
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>>
    getCountByCategory(
            @RequestParam Category category) {
        long count = questionService
                .countByCategory(category);
        return ResponseEntity.ok(
                ApiResponse.success(
                        Map.of("count", count),
                        "Question count"));
    }
}