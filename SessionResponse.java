package com.AI.CodeAssistant.dto.response;

import com.AI.CodeAssistant.model.Session.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long sessionId;
    private InterviewType interviewType;
    private SessionStatus status;
    private String targetRole;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Double overallScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private QuestionResponse currentQuestion;
}