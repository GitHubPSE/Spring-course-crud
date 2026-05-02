package com.assignment.course.global.exception;

import jakarta.persistence.LockTimeoutException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(GlobalException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(Map.of(
                        "status", errorCode.getStatus().value(),
                        "message", errorCode.getMessage()
                ));
    }

    // 비관적 락 타임아웃 (10초 초과 시)
    @ExceptionHandler({LockTimeoutException.class, CannotAcquireLockException.class})
    public ResponseEntity<Map<String, Object>> handleLockTimeoutException(Exception e) {
        ErrorCode errorCode = ErrorCode.LOCK_TIMEOUT;
        return ResponseEntity.status(errorCode.getStatus())
                .body(Map.of(
                        "status", errorCode.getStatus().value(),
                        "message", errorCode.getMessage()
                ));
    }
}
