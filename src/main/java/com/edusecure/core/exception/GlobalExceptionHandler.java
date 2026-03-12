package com.edusecure.core.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── BusinessException (toutes nos exceptions métier) ───────────
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("BusinessException : [{}] {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.of(ex.getMessage(), ex.getErrorCode()));
    }

    // ─── Validation @Valid → erreurs champ par champ ────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach((FieldError e) -> errors.put(e.getField(), e.getDefaultMessage()));

        log.warn("Validation échouée : {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Données invalides")
                        .errorCode("VALIDATION_ERROR")
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── Accès refusé (rôle insuffisant) ────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {
        log.warn("Accès refusé : {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("Accès refusé", "FORBIDDEN"));
    }

    // ─── Mauvais identifiants ────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Identifiants invalides", "UNAUTHORIZED"));
    }

    // ─── Erreur inattendue ───────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log complet côté serveur
        log.error("Erreur inattendue", ex);

        // Message générique côté client — ne jamais exposer le détail technique
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Erreur interne du serveur", "INTERNAL_ERROR"));
    }

    // ─── Format de réponse d'erreur unifié ──────────────────────────
    @Getter
    @Builder
    public static class ErrorResponse {

        private final boolean             success;
        private final String              message;
        private final String              errorCode;
        private final Map<String, String> errors;     // champs invalides (@Valid)
        private final LocalDateTime       timestamp;

        // Factory rapide pour les cas simples
        public static ErrorResponse of(String message, String errorCode) {
            return ErrorResponse.builder()
                    .success(false)
                    .message(message)
                    .errorCode(errorCode)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}