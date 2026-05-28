package com.AI.CodeAssistant.repository;

import com.AI.CodeAssistant.model.Answer;
import com.AI.CodeAssistant.model.Question.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository
        extends JpaRepository<Answer, Long> {

    List<Answer> findBySessionIdOrderByCreatedAtAsc(
            Long sessionId);

    Optional<Answer> findBySessionIdAndQuestionId(
            Long sessionId, Long questionId);

    Long countBySessionId(Long sessionId);

    @Query("SELECT AVG(a.overallScore) FROM Answer a " +
            "WHERE a.session.id = :sessionId " +
            "AND a.overallScore IS NOT NULL")
    Double getAverageScoreBySession(
            @Param("sessionId") Long sessionId);

    @Query("SELECT AVG(a.technicalScore) FROM Answer a " +
            "WHERE a.session.user.id = :userId " +
            "AND a.technicalScore IS NOT NULL")
    Double getAvgTechnicalScore(
            @Param("userId") Long userId);

    @Query("SELECT AVG(a.communicationScore) FROM Answer a " +
            "WHERE a.session.user.id = :userId " +
            "AND a.communicationScore IS NOT NULL")
    Double getAvgCommunicationScore(
            @Param("userId") Long userId);

    @Query("SELECT AVG(a.overallScore) FROM Answer a " +
            "WHERE a.session.user.id = :userId " +
            "AND a.question.category = :category " +
            "AND a.overallScore IS NOT NULL")
    Double getAvgScoreByCategory(
            @Param("userId") Long userId,
            @Param("category") Category category);

    @Query("SELECT a.question.category, " +
            "AVG(a.overallScore) " +
            "FROM Answer a " +
            "WHERE a.session.user.id = :userId " +
            "AND a.overallScore IS NOT NULL " +
            "GROUP BY a.question.category " +
            "ORDER BY AVG(a.overallScore) ASC")
    List<Object[]> getScoreByCategory(
            @Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM Answer a " +
            "WHERE a.session.user.id = :userId")
    Long getTotalAnswersByUser(
            @Param("userId") Long userId);
}