package com.AI.CodeAssistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_number")
    private Integer weekNumber;

    @Column(name = "avg_score")
    private Double avgScore;

    @Column(name = "sessions_count")
    @Builder.Default
    private Integer sessionsCount = 0;

    @Column(name = "improvement_percent")
    private Double improvementPercent;

    @Column(name = "strongest_area")
    private String strongestArea;

    @Column(name = "weakest_area")
    private String weakestArea;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}