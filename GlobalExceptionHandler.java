package com.AI.CodeAssistant.config;
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//      Must be in config package ✅

import com.AI.CodeAssistant.dto.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.security.authentication
        .BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind
        .MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>>
    handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field =
                            ((FieldError) error).getField();
                    String message =
                            error.getDefaultMessage();
                    errors.put(field, message);
                });

        return ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleBadCredentials(
            BadCredentialsException ex) {
        return ApiResponse.error(
                "Invalid email or password");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleRuntime(
            RuntimeException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneral(
            Exception ex) {
        return ApiResponse.error(
                "Something went wrong: "
                        + ex.getMessage());
    }
}