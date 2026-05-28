package com.AI.CodeAssistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text",
            nullable = false,
            columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "target_company")
    private String targetCompany;

    @Column(name = "expected_keywords",
            columnDefinition = "TEXT")
    private String expectedKeywords;

    @Column(name = "ideal_answer",
            columnDefinition = "TEXT")
    private String idealAnswer;

    @Column(name = "time_limit_seconds")
    @Builder.Default
    private Integer timeLimitSeconds = 120;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "question",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Answer> answers;

    // ✅ All categories including new ones
    public enum Category {
        DSA,
        SYSTEM_DESIGN,
        BEHAVIORAL,
        HR,
        JAVA,        // ✅ Added
        SPRING_BOOT, // ✅ Added
        DATABASE     // ✅ Added
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
}