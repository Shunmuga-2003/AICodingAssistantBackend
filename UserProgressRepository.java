package com.AI.CodeAssistant.repository;

import com.AI.CodeAssistant.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository
        extends JpaRepository<UserProgress, Long> {

    List<UserProgress> findByUserIdOrderByWeekNumberDesc(
            Long userId);

    Optional<UserProgress> findByUserIdAndWeekNumber(
            Long userId, Integer weekNumber);

    @Query("SELECT AVG(up.improvementPercent) " +
            "FROM UserProgress up " +
            "WHERE up.user.id = :userId " +
            "AND up.improvementPercent IS NOT NULL")
    Double getAverageImprovement(
            @Param("userId") Long userId);
}