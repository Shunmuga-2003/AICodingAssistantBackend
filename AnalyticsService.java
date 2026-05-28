package com.AI.CodeAssistant.service;

import com.AI.CodeAssistant.dto.response.ProgressResponse;
import com.AI.CodeAssistant.model.*;
import com.AI.CodeAssistant.model.Session.InterviewType;
import com.AI.CodeAssistant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final AnswerRepository answerRepository;

    public ProgressResponse getProgress(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        Long userId = user.getId();

        Long totalSessions = sessionRepository
                .countByUserId(userId);
        Long totalAnswers = answerRepository
                .getTotalAnswersByUser(userId);
        Double overallAvg = sessionRepository
                .getAverageScoreByUserId(userId);

        Double dsaAvg = sessionRepository
                .getAverageScoreByType(
                        userId, InterviewType.DSA);
        Double sdAvg = sessionRepository
                .getAverageScoreByType(
                        userId, InterviewType.SYSTEM_DESIGN);
        Double behavAvg = sessionRepository
                .getAverageScoreByType(
                        userId, InterviewType.BEHAVIORAL);
        Double hrAvg = sessionRepository
                .getAverageScoreByType(
                        userId, InterviewType.HR);

        List<Object[]> categoryScores = answerRepository
                .getScoreByCategory(userId);

        String weakestArea  = "System Design";
        String strongestArea = "Behavioral";

        if (!categoryScores.isEmpty()) {
            weakestArea = categoryScores
                    .get(0)[0].toString();
            strongestArea = categoryScores
                    .get(categoryScores.size() - 1)[0]
                    .toString();
        }

        List<Session> recentSessions = sessionRepository
                .findRecentSessions(userId,
                        LocalDateTime.now()
                                .minusDays(30));

        List<ProgressResponse.SessionSummary> summaries =
                recentSessions.stream()
                        .map(s -> ProgressResponse
                                .SessionSummary.builder()
                                .sessionId(s.getId())
                                .interviewType(
                                        s.getInterviewType()
                                                .name())
                                .score(s.getOverallScore())
                                .questionsAnswered(
                                        s.getAnsweredQuestions())
                                .date(s.getStartedAt() != null
                                        ? s.getStartedAt()
                                        .toLocalDate()
                                        .toString()
                                        : "")
                                .build())
                        .collect(Collectors.toList());

        List<Object[]> weeklyData = sessionRepository
                .getWeeklyProgress(userId);

        List<ProgressResponse.WeeklyScore> weeklyScores =
                weeklyData.stream()
                        .map(row -> ProgressResponse
                                .WeeklyScore.builder()
                                .week(((Number) row[0])
                                        .intValue())
                                .avgScore(row[2] != null
                                        ? ((Number) row[2])
                                        .doubleValue()
                                        : 0.0)
                                .sessionsCount(
                                        ((Number) row[1])
                                                .intValue())
                                .build())
                        .collect(Collectors.toList());

        return ProgressResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .totalSessions(totalSessions.intValue())
                .totalQuestionsAnswered(
                        totalAnswers != null
                                ? totalAnswers.intValue() : 0)
                .overallAverageScore(
                        overallAvg != null
                                ? Math.round(overallAvg * 10.0)
                                / 10.0 : 0.0)
                .dsaAvgScore(dsaAvg)
                .systemDesignAvgScore(sdAvg)
                .behavioralAvgScore(behavAvg)
                .hrAvgScore(hrAvg)
                .strongestArea(strongestArea)
                .weakestArea(weakestArea)
                .recentSessions(summaries)
                .weeklyProgress(weeklyScores)
                .build();
    }
}