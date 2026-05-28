package com.AI.CodeAssistant.service;

import com.AI.CodeAssistant.dto.request.AnswerRequest;
import com.AI.CodeAssistant.dto.response.FeedbackResponse;
import com.AI.CodeAssistant.dto.response.QuestionResponse;
import com.AI.CodeAssistant.model.*;
import com.AI.CodeAssistant.repository.AnswerRepository;
import com.AI.CodeAssistant.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final SessionRepository sessionRepository;
    private final QuestionService questionService;
    private final AIService aiService;
    private final SessionService sessionService;
    private final SpeechService speechService;

    public FeedbackResponse submitTextAnswer(
            Long sessionId,
            AnswerRequest request,
            String email) {

        String transcript = request.getTextAnswer();

        if (transcript == null || transcript.isBlank()) {
            throw new RuntimeException(
                    "Answer cannot be empty");
        }

        return processAnswer(
                sessionId,
                request.getQuestionId(),
                transcript,
                null,
                request.getDurationSeconds(),
                email);
    }

    public FeedbackResponse submitVoiceAnswer(
            Long sessionId,
            Long questionId,
            MultipartFile audioFile,
            Integer durationSeconds,
            String email) {

        speechService.validateAudioFile(audioFile);
        String transcript = speechService
                .transcribe(audioFile);

        return processAnswer(
                sessionId,
                questionId,
                transcript,
                audioFile.getOriginalFilename(),
                durationSeconds,
                email);
    }

    public List<Answer> getSessionAnswers(
            Long sessionId) {
        return answerRepository
                .findBySessionIdOrderByCreatedAtAsc(
                        sessionId);
    }

    private int countFillerWords(String text) {
        if (text == null) return 0;
        List<String> fillers = Arrays.asList(
                "um", "uh", "like", "you know",
                "basically", "actually", "literally",
                "sort of", "kind of", "i mean"
        );
        String lower = text.toLowerCase();
        int count = 0;
        for (String filler : fillers) {
            int index = 0;
            while ((index = lower.indexOf(
                    filler, index)) != -1) {
                count++;
                index += filler.length();
            }
        }
        return count;
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
    // Replace processAnswer method only

    public FeedbackResponse processAnswer(
            Long sessionId,
            Long questionId,
            String transcript,
            String audioPath,
            Integer durationSeconds,
            String email) {

        Session session = sessionService
                .getById(sessionId);
        Question question = questionService
                .getById(questionId);

        int fillerCount = countFillerWords(transcript);
        int wordCount = countWords(transcript);

        FeedbackResponse feedback = aiService
                .evaluateAnswer(
                        question.getQuestionText(),
                        transcript,
                        session.getTargetRole() != null
                                ? session.getTargetRole()
                                : "SDE2");

        // Save answer
        Answer answer = Answer.builder()
                .session(session)
                .question(question)
                .transcript(transcript)
                .audioPath(audioPath)
                .technicalScore(
                        feedback.getTechnicalScore())
                .communicationScore(
                        feedback.getCommunicationScore())
                .structureScore(
                        feedback.getStructureScore())
                .completenessScore(
                        feedback.getCompletenessScore())
                .overallScore(
                        feedback.getOverallScore())
                .llmFeedback(
                        feedback.getOverallFeedback())
                .betterAnswer(
                        feedback.getBetterAnswer())
                .missingPoints(
                        feedback.getMissingPoints() != null
                                ? String.join(",",
                                feedback.getMissingPoints())
                                : "")
                .fillerWordCount(fillerCount)
                .wordCount(wordCount)
                .durationSeconds(durationSeconds)
                .build();

        Answer saved = answerRepository.save(answer);

        // ✅ Increment answered
        sessionService.incrementAnswered(sessionId);

        // ✅ Get updated session
        Session updated = sessionService
                .getById(sessionId);

        int answeredNow =
                updated.getAnsweredQuestions();
        int totalQ = updated.getTotalQuestions();
        boolean isComplete = answeredNow >= totalQ;

        feedback.setAnswerId(saved.getId());
        feedback.setQuestionId(question.getId());
        feedback.setQuestionText(
                question.getQuestionText());
        feedback.setSessionCompleted(isComplete);
        feedback.setFillerWordCount(fillerCount);
        feedback.setWordCount(wordCount);

        // ✅ Set next question using stored IDs
        if (!isComplete) {
            try {
                Question nextQ = sessionService
                        .getQuestionByIndex(
                                updated, answeredNow);

                QuestionResponse nextResponse =
                        QuestionResponse.builder()
                                .questionId(nextQ.getId())
                                .questionText(
                                        nextQ.getQuestionText())
                                .category(nextQ.getCategory())
                                .difficulty(nextQ.getDifficulty())
                                .timeLimitSeconds(
                                        nextQ.getTimeLimitSeconds())
                                .questionNumber(answeredNow + 1)
                                .totalQuestions(totalQ)
                                .build();

                feedback.setNextQuestion(nextResponse);

                log.info("Next question: {} ({}/{})",
                        nextQ.getId(),
                        answeredNow + 1,
                        totalQ);

            } catch (Exception e) {
                log.error("Could not get next Q: {}",
                        e.getMessage());
            }
        } else {
            // Session complete
            Double sessionScore = answerRepository
                    .getAverageScoreBySession(sessionId);
            feedback.setSessionOverallScore(
                    sessionScore);
            log.info("Session complete! Score: {}",
                    sessionScore);
        }

        return feedback;
    }

}