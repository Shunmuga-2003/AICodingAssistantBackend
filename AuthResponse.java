package com.AI.CodeAssistant.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private String targetRole;
    private String targetCompany;
    private String message;
}