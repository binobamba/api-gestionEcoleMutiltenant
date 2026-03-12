package com.edusecure.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String     errorCode;

    // Constructeur de base
    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    // ─── Méthodes factory — évitent de répéter HttpStatus partout ───

    public static BusinessException notFound(String message) {
        return new BusinessException(
                message,
                HttpStatus.NOT_FOUND,
                "NOT_FOUND"
        );
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(
                message,
                HttpStatus.CONFLICT,
                "CONFLICT"
        );
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(
                message,
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST"
        );
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(
                message,
                HttpStatus.FORBIDDEN,
                "FORBIDDEN"
        );
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(
                message,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED"
        );
    }

    public static BusinessException tenantNotFound(String tenantId) {
        return new BusinessException(
                "Tenant introuvable : " + tenantId,
                HttpStatus.FORBIDDEN,
                "INVALID_TENANT"
        );
    }
}