package com.AI.CodeAssistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "interview_type",
            nullable = false,
            length = 50)
    private InterviewType interviewType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status =
            SessionStatus.ACTIVE;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "total_questions")
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "answered_questions")
    @Builder.Default
    private Integer answeredQuestions = 0;

    // ✅ Store comma separated question IDs
    // e.g: "1,5,12"
    @Column(name = "question_ids",
            columnDefinition = "TEXT")
    private String questionIds;

    @CreationTimestamp
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Answer> answers;

    public enum InterviewType {
        DSA,
        SYSTEM_DESIGN,
        BEHAVIORAL,
        HR,
        JAVA,
        SPRING_BOOT,
        DATABASE,
        MIXED
    }

    public enum SessionStatus {
        ACTIVE,
        COMPLETED,
        ABANDONED
    }
}