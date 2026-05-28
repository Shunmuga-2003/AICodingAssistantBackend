package com.AI.CodeAssistant.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    private Long answerId;
    private Long questionId;
    private String questionText;
    private String transcript;

    private Double technicalScore;
    private Double communicationScore;
    private Double structureScore;
    private Double completenessScore;
    private Double overallScore;

    private String overallFeedback;
    private String betterAnswer;
    private List<String> missingPoints;
    private List<String> strongPoints;

    private Integer fillerWordCount;
    private List<String> fillerWordsFound;
    private Integer wordCount;
    private Double speakingPace;
    private Integer durationSeconds;

    private Boolean sessionCompleted;
    private QuestionResponse nextQuestion;
    private Double sessionOverallScore;
}