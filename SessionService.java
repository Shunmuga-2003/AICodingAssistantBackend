package com.AI.CodeAssistant.service;

import com.AI.CodeAssistant.dto.request.SessionRequest;
import com.AI.CodeAssistant.dto.response.QuestionResponse;
import com.AI.CodeAssistant.dto.response.SessionResponse;
import com.AI.CodeAssistant.model.Question;
import com.AI.CodeAssistant.model.Session;
import com.AI.CodeAssistant.model.Session.SessionStatus;
import com.AI.CodeAssistant.model.User;
import com.AI.CodeAssistant.repository.AnswerRepository;
import com.AI.CodeAssistant.repository.SessionRepository;
import com.AI.CodeAssistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final AnswerRepository answerRepository;

    // ─── Start Session ────────────────────────────
    public SessionResponse startSession(
            String email,
            SessionRequest request) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        // End any active session first
        sessionRepository
                .findByUserIdAndStatus(
                        user.getId(),
                        SessionStatus.ACTIVE)
                .ifPresent(s -> {
                    s.setStatus(
                            SessionStatus.COMPLETED);
                    s.setEndedAt(
                            LocalDateTime.now());
                    sessionRepository.save(s);
                });

        // ✅ Get exact number of questions
        int count = request.getNumberOfQuestions();
        List<Question> questions = questionService
                .getQuestionsForSession(
                        request.getInterviewType(),
                        count);

        if (questions.isEmpty()) {
            throw new RuntimeException(
                    "No questions found for: "
                            + request.getInterviewType());
        }

        // ✅ Store only requested count
        int actualCount = Math.min(
                count, questions.size());
        questions = questions.subList(
                0, actualCount);

        // ✅ Store question IDs as comma string
        // e.g: "1,5,12"
        String questionIds = questions.stream()
                .map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(","));

        log.info("Session questions: {}",
                questionIds);

        // Create session
        Session session = Session.builder()
                .user(user)
                .interviewType(
                        request.getInterviewType())
                .targetRole(
                        request.getTargetRole() != null
                                ? request.getTargetRole()
                                : user.getTargetRole())
                .status(SessionStatus.ACTIVE)
                .totalQuestions(actualCount)
                .answeredQuestions(0)
                .questionIds(questionIds) // ✅ Saved
                .build();

        Session saved =
                sessionRepository.save(session);

        // First question
        Question first = questions.get(0);

        return SessionResponse.builder()
                .sessionId(saved.getId())
                .interviewType(
                        saved.getInterviewType())
                .status(saved.getStatus())
                .targetRole(saved.getTargetRole())
                .totalQuestions(
                        saved.getTotalQuestions())
                .answeredQuestions(0)
                .startedAt(saved.getStartedAt())
                .currentQuestion(
                        mapToQuestionResponse(
                                first, 1,
                                actualCount))
                .build();
    }

    // ─── Get next question ────────────────────────
    public QuestionResponse getNextQuestion(
            Long sessionId, String email) {

        Session session = getValidSession(
                sessionId, email);

        int answered =
                session.getAnsweredQuestions();
        int total = session.getTotalQuestions();

        if (answered >= total) {
            throw new RuntimeException(
                    "All questions answered.");
        }

        // ✅ Get question from stored IDs
        Question next = getQuestionByIndex(
                session, answered);

        return mapToQuestionResponse(
                next, answered + 1, total);
    }

    // ─── End session ──────────────────────────────
    public SessionResponse endSession(
            Long sessionId, String email) {

        Session session = getValidSession(
                sessionId, email);

        Double avgScore = answerRepository
                .getAverageScoreBySession(sessionId);

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setOverallScore(
                avgScore != null ? avgScore : 0.0);

        Session saved =
                sessionRepository.save(session);

        return SessionResponse.builder()
                .sessionId(saved.getId())
                .interviewType(
                        saved.getInterviewType())
                .status(saved.getStatus())
                .targetRole(saved.getTargetRole())
                .totalQuestions(
                        saved.getTotalQuestions())
                .answeredQuestions(
                        saved.getAnsweredQuestions())
                .overallScore(saved.getOverallScore())
                .startedAt(saved.getStartedAt())
                .endedAt(saved.getEndedAt())
                .build();
    }

    // ─── Get history ──────────────────────────────
    public List<Session> getHistory(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
        return sessionRepository
                .findByUserIdOrderByStartedAtDesc(
                        user.getId());
    }

    // ─── Get by ID ────────────────────────────────
    public Session getById(Long sessionId) {
        return sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found: "
                                        + sessionId));
    }

    // ─── Increment answered ───────────────────────
    public void incrementAnswered(Long sessionId) {
        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"));
        session.setAnsweredQuestions(
                session.getAnsweredQuestions() + 1);
        sessionRepository.save(session);
    }

    // ─── Get target role ──────────────────────────
    public String getTargetRole(Long sessionId) {
        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"));
        return session.getTargetRole() != null
                ? session.getTargetRole() : "SDE2";
    }

    // ✅ Get question by index from stored IDs
    public Question getQuestionByIndex(
            Session session, int index) {

        String questionIds = session.getQuestionIds();

        if (questionIds == null ||
                questionIds.isBlank()) {
            throw new RuntimeException(
                    "No questions stored in session");
        }

        List<Long> ids = Arrays
                .stream(questionIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (index >= ids.size()) {
            throw new RuntimeException(
                    "Question index out of range");
        }

        Long questionId = ids.get(index);
        return questionService.getById(questionId);
    }

    // ─── Validate session ─────────────────────────
    private Session getValidSession(
            Long sessionId, String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found"));

        if (!session.getUser().getId()
                .equals(user.getId())) {
            throw new RuntimeException(
                    "Unauthorized access");
        }

        return session;
    }

    // ─── Map to response ──────────────────────────
    private QuestionResponse mapToQuestionResponse(
            Question question,
            int questionNumber,
            int totalQuestions) {

        return QuestionResponse.builder()
                .questionId(question.getId())
                .questionText(
                        question.getQuestionText())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .timeLimitSeconds(
                        question.getTimeLimitSeconds())
                .questionNumber(questionNumber)
                .totalQuestions(totalQuestions)
                .build();
    }
}