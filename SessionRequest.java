package com.AI.CodeAssistant.dto.request;

import com.AI.CodeAssistant.model.Session
        .InterviewType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    @NotNull(message =
            "Interview type is required")
    private InterviewType interviewType;

    private String targetRole;

    @Min(value = 1,
            message = "Minimum 1 question")
    @Max(value = 20,
            message = "Maximum 20 questions")
    private Integer numberOfQuestions = 5;
}