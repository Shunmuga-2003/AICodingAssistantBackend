package com.AI.CodeAssistant.dto.response;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponse {

    private String transcribedText;     // STT result
    private String aiFeedback;          // AI evaluation
    private Double score;               // answer score
    private String audioFeedbackUrl;    // TTS audio URL
    private Boolean sessionComplete;
    private QuestionResponse nextQuestion;
}
