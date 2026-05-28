package com.AI.CodeAssistant.dto.response;

import com.AI.CodeAssistant.model.Question.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private Long questionId;
    private String questionText;
    private Category category;
    private Difficulty difficulty;
    private Integer timeLimitSeconds;
    private Integer questionNumber;
    private Integer totalQuestions;
}