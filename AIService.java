package com.AI.CodeAssistant.service;

import com.AI.CodeAssistant.dto.response.FeedbackResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AIService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    // ✅ Read model from properties
    @Value("${groq.model}")
    private String groqModel;

    private final ObjectMapper objectMapper =
            new ObjectMapper();
    private final RestTemplate restTemplate =
            new RestTemplate();

    public FeedbackResponse evaluateAnswer(
            String questionText,
            String transcript,
            String targetRole) {

        String prompt = buildPrompt(
                questionText, transcript, targetRole);

        try {
            // ✅ See if key is loaded
            log.info("=== GROQ API CALL START ===");
            log.info("API Key: {}",
                    groqApiKey != null
                            ? groqApiKey.substring(0, 15) + "..."
                            : "NULL ❌");
            log.info("API URL: {}", groqApiUrl);
            log.info("Model: {}", groqModel);

            String rawResponse = callGroqAPI(prompt);

            log.info("=== GROQ RESPONSE ===");
            log.info("Response: {}", rawResponse);

            return parseAIResponse(rawResponse, transcript);

        } catch (Exception e) {
            log.error("=== GROQ FAILED ===");
            log.error("Error: {}", e.getMessage());
            log.error("Cause: {}",
                    e.getCause() != null
                            ? e.getCause().getMessage()
                            : "No cause");
            return buildFallbackFeedback(transcript);
        }
    }

    private String buildPrompt(
            String question,
            String answer,
            String role) {

        return """
            You are a senior interviewer at FAANG
            interviewing for %s position.

            Evaluate this answer strictly and fairly.

            QUESTION: %s
            ANSWER: %s

            Respond ONLY in this exact JSON format
            with no extra text outside JSON:
            {
                "technical_score": 7.5,
                "communication_score": 6.0,
                "structure_score": 7.0,
                "completeness_score": 6.5,
                "overall_score": 6.8,
                "overall_feedback": "feedback here",
                "strong_points": ["point1", "point2"],
                "missing_points": ["point1", "point2"],
                "better_answer": "better answer here",
                "filler_words_found": ["um", "like"],
                "filler_word_count": 3
            }

            Scoring guide (0-10):
            - technical_score: technically correct?
            - communication_score: clear and articulate?
            - structure_score: well organized?
            - completeness_score: covers all points?
            - overall_score: weighted average
            """.formatted(role, question, answer);
    }

    private String callGroqAPI(String prompt)
            throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization",
                "Bearer " + groqApiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        // ✅ Use model from properties
        body.put("model", groqModel);
        body.put("messages", List.of(message));
        body.put("max_tokens", 1000);
        body.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        groqApiUrl, entity, String.class);

        return response.getBody();
    }

    private FeedbackResponse parseAIResponse(
            String rawResponse,
            String transcript) throws Exception {

        JsonNode root = objectMapper
                .readTree(rawResponse);

        String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        // ✅ Clean JSON if AI adds extra text
        content = content.trim();
        if (content.contains("{")) {
            content = content.substring(
                    content.indexOf("{"),
                    content.lastIndexOf("}") + 1);
        }

        JsonNode feedback = objectMapper
                .readTree(content);

        List<String> missingPoints = new ArrayList<>();
        feedback.path("missing_points").forEach(
                node -> missingPoints.add(node.asText()));

        List<String> strongPoints = new ArrayList<>();
        feedback.path("strong_points").forEach(
                node -> strongPoints.add(node.asText()));

        List<String> fillerWords = new ArrayList<>();
        feedback.path("filler_words_found").forEach(
                node -> fillerWords.add(node.asText()));

        return FeedbackResponse.builder()
                .transcript(transcript)
                .technicalScore(feedback
                        .path("technical_score")
                        .asDouble())
                .communicationScore(feedback
                        .path("communication_score")
                        .asDouble())
                .structureScore(feedback
                        .path("structure_score")
                        .asDouble())
                .completenessScore(feedback
                        .path("completeness_score")
                        .asDouble())
                .overallScore(feedback
                        .path("overall_score")
                        .asDouble())
                .overallFeedback(feedback
                        .path("overall_feedback")
                        .asText())
                .strongPoints(strongPoints)
                .missingPoints(missingPoints)
                .betterAnswer(feedback
                        .path("better_answer")
                        .asText())
                .fillerWordsFound(fillerWords)
                .fillerWordCount(feedback
                        .path("filler_word_count")
                        .asInt())
                .wordCount(countWords(transcript))
                .build();
    }

    private FeedbackResponse buildFallbackFeedback(
            String transcript) {
        return FeedbackResponse.builder()
                .transcript(transcript)
                .technicalScore(5.0)
                .communicationScore(5.0)
                .structureScore(5.0)
                .completenessScore(5.0)
                .overallScore(5.0)
                .overallFeedback(
                        "AI evaluation temporarily " +
                                "unavailable. Please try again.")
                .missingPoints(List.of())
                .strongPoints(List.of())
                .wordCount(countWords(transcript))
                .fillerWordCount(0)
                .build();
    }

    private Integer countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}