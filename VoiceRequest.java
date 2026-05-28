package com.AI.CodeAssistant.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceRequest {

    private Long sessionId;
    private Long questionId;
    private String transcribedText;    // text from STT
    private Integer timeTakenSeconds;
}