package com.AI.CodeAssistant.service;

import com.AI.CodeAssistant.model.Question;
import com.AI.CodeAssistant.model.Question.Category;
import com.AI.CodeAssistant.model.Question.Difficulty;
import com.AI.CodeAssistant.model.Session.InterviewType;
import com.AI.CodeAssistant.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository
            questionRepository;

    public Question getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found: "
                                        + id));
    }

    // ✅ Use String switch to avoid enum error
    public List<Question> getQuestionsForSession(
            InterviewType type, int count) {

        String category = type.name();

        return switch (category) {
            case "DSA" ->
                    questionRepository
                            .findRandomByCategory(
                                    "DSA", count);
            case "SYSTEM_DESIGN" ->
                    questionRepository
                            .findRandomByCategory(
                                    "SYSTEM_DESIGN", count);
            case "BEHAVIORAL" ->
                    questionRepository
                            .findRandomByCategory(
                                    "BEHAVIORAL", count);
            case "HR" ->
                    questionRepository
                            .findRandomByCategory(
                                    "HR", count);
            case "JAVA" ->
                    questionRepository
                            .findRandomByCategory(
                                    "JAVA", count);
            case "SPRING_BOOT" ->
                    questionRepository
                            .findRandomByCategory(
                                    "SPRING_BOOT", count);
            case "DATABASE" ->
                    questionRepository
                            .findRandomByCategory(
                                    "DATABASE", count);
            default ->
                    questionRepository
                            .findRandomQuestions(count);
        };
    }

    public List<Question> getByCategory(
            Category category) {
        return questionRepository
                .findByCategoryAndIsActiveTrue(
                        category);
    }

    public List<Question> getByDifficulty(
            Difficulty difficulty) {
        return questionRepository
                .findByDifficultyAndIsActiveTrue(
                        difficulty);
    }

    public List<Question> search(String keyword) {
        return questionRepository
                .searchByKeyword(keyword);
    }

    public List<Question> getAll() {
        return questionRepository.findAll();
    }

    public Question save(Question question) {
        return questionRepository.save(question);
    }

    // ✅ NEW: count active questions per category
    public long countByCategory(Category category) {
        return questionRepository
                .countByCategoryAndIsActiveTrue(
                        category);
    }
}