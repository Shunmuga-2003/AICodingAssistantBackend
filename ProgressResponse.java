package com.AI.CodeAssistant.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponse {

    private Long userId;
    private String userName;
    private Integer totalSessions;
    private Integer totalQuestionsAnswered;
    private Double overallAverageScore;
    private Double improvementPercent;
    private Double dsaAvgScore;
    private Double systemDesignAvgScore;
    private Double behavioralAvgScore;
    private Double hrAvgScore;
    private String strongestArea;
    private String weakestArea;
    private List<String> topStrengths;
    private List<String> areasToImprove;
    private List<SessionSummary> recentSessions;
    private List<WeeklyScore> weeklyProgress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSummary {
        private Long sessionId;
        private String interviewType;
        private Double score;
        private Integer questionsAnswered;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyScore {
        private Integer week;
        private Double avgScore;
        private Integer sessionsCount;
    }
}