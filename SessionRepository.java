package com.AI.CodeAssistant.repository;

import com.AI.CodeAssistant.model.Session;
import com.AI.CodeAssistant.model.Session.InterviewType;
import com.AI.CodeAssistant.model.Session.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository
        extends JpaRepository<Session, Long> {

    List<Session> findByUserIdOrderByStartedAtDesc(
            Long userId);

    Optional<Session> findByUserIdAndStatus(
            Long userId, SessionStatus status);

    Long countByUserId(Long userId);

    @Query("SELECT AVG(s.overallScore) FROM Session s " +
            "WHERE s.user.id = :userId " +
            "AND s.status = 'COMPLETED' " +
            "AND s.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(
            @Param("userId") Long userId);

    @Query("SELECT AVG(s.overallScore) FROM Session s " +
            "WHERE s.user.id = :userId " +
            "AND s.interviewType = :type " +
            "AND s.status = 'COMPLETED'")
    Double getAverageScoreByType(
            @Param("userId") Long userId,
            @Param("type") InterviewType type);

    @Query("SELECT s FROM Session s " +
            "WHERE s.user.id = :userId " +
            "AND s.startedAt >= :fromDate " +
            "ORDER BY s.startedAt DESC")
    List<Session> findRecentSessions(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT WEEK(s.startedAt), COUNT(s), " +
            "AVG(s.overallScore) " +
            "FROM Session s " +
            "WHERE s.user.id = :userId " +
            "AND s.status = 'COMPLETED' " +
            "GROUP BY WEEK(s.startedAt) " +
            "ORDER BY WEEK(s.startedAt) DESC")
    List<Object[]> getWeeklyProgress(
            @Param("userId") Long userId);
}