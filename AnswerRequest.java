package com.AI.CodeAssistant.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    private String textAnswer;
    private Integer durationSeconds;
}