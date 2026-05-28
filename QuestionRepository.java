package com.AI.CodeAssistant.repository;

import com.AI.CodeAssistant.model.Question;
import com.AI.CodeAssistant.model.Question.Category;
import com.AI.CodeAssistant.model.Question.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    List<Question> findByCategoryAndIsActiveTrue(
            Category category);

    List<Question> findByDifficultyAndIsActiveTrue(
            Difficulty difficulty);

    List<Question> findByCategoryAndDifficultyAndIsActiveTrue(
            Category category, Difficulty difficulty);

    // ✅ NEW: count active questions per category
    long countByCategoryAndIsActiveTrue(
            Category category);

    @Query(value = "SELECT * FROM questions " +
            "WHERE category = :category " +
            "AND is_active = true " +
            "ORDER BY RAND() LIMIT :limit",
            nativeQuery = true)
    List<Question> findRandomByCategory(
            @Param("category") String category,
            @Param("limit") int limit);

    @Query(value = "SELECT * FROM questions " +
            "WHERE is_active = true " +
            "ORDER BY RAND() LIMIT :limit",
            nativeQuery = true)
    List<Question> findRandomQuestions(
            @Param("limit") int limit);

    @Query("SELECT q FROM Question q " +
            "WHERE q.isActive = true " +
            "AND LOWER(q.questionText) " +
            "LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Question> searchByKeyword(
            @Param("keyword") String keyword);
}