package com.AI.CodeAssistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "transcript",
            columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "technical_score")
    private Double technicalScore;

    @Column(name = "communication_score")
    private Double communicationScore;

    @Column(name = "structure_score")
    private Double structureScore;

    @Column(name = "completeness_score")
    private Double completenessScore;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "llm_feedback",
            columnDefinition = "TEXT")
    private String llmFeedback;

    @Column(name = "better_answer",
            columnDefinition = "TEXT")
    private String betterAnswer;

    @Column(name = "missing_points",
            columnDefinition = "TEXT")
    private String missingPoints;

    @Column(name = "filler_word_count")
    @Builder.Default
    private Integer fillerWordCount = 0;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "speaking_pace")
    private Double speakingPace;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}