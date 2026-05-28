package com.AI.CodeAssistant.repository;

import com.AI.CodeAssistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(" +
            "CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(" +
            "CONCAT('%', :keyword, '%'))")
    List<User> searchByNameOrEmail(
            @Param("keyword") String keyword);
}