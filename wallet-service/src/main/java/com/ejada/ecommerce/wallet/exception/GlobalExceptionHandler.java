package com.ejada.ecommerce.wallet.exception;

import com.ejada.ecommerce.wallet.security.MissingIdentityHeaderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ProblemDetail handleNotFound(WalletNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Wallet not found");
        problem.setType(URI.create("urn:ejada:wallet:not-found"));
        return problem;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        problem.setTitle("Insufficient funds");
        problem.setType(URI.create("urn:ejada:wallet:insufficient-funds"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Validation error");
        problem.setType(URI.create("urn:ejada:wallet:validation"));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MissingIdentityHeaderException.class)
    public ProblemDetail handleMissingIdentity(MissingIdentityHeaderException e) {
        log.error("Identity header missing — gateway bypassed: {}", e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Service misconfiguration");
        problem.setTitle("Identity unavailable");
        problem.setType(URI.create("urn:ejada:wallet:identity-missing"));
        return problem;
    }
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof InsufficientFundsException ife) {
                return handleInsufficientFunds(ife);
            }
            cause = cause.getCause();
        }
        log.error("Unhandled exception", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        problem.setTitle("Internal error");
        return problem;
    }

    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    public ProblemDetail handleLockFailure(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Concurrent modification, please retry");
        problem.setTitle("Conflict");
        problem.setType(URI.create("urn:ejada:wallet:concurrent-modification"));
        return problem;
    }
}