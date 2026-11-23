package com.helpdesk.api.core.exceptions;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();
        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for one or more fields"
        );
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler({IllegalArgumentException.class, DataIntegrityViolationException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage() == null ? "Invalid request" : ex.getMessage()
        );
        problem.setTitle("Invalid Request");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Wrong username or password. The message is deliberately the same for both. */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        problem.setTitle("Unauthorized");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerified(EmailNotVerifiedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Email Not Verified");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication failed");
        problem.setTitle("Unauthorized");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** @PreAuthorize denials are thrown inside the controller, so they reach this advice, not the filter chain. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
        problem.setTitle("Forbidden");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"
        );
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
