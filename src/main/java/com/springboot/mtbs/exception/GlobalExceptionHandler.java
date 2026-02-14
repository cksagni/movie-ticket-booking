package com.springboot.mtbs.exception;

import com.springboot.mtbs.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception, request, Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception, request, Map.of());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception, request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                details,
                request.getHeader("X-Trace-Id"),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_ERROR",
                "Unexpected server error",
                Map.of(),
                request.getHeader("X-Trace-Id"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                ApiException exception,
                                                HttpServletRequest request,
                                                Map<String, Object> details) {
        ErrorResponse response = new ErrorResponse(
                exception.getCode(),
                exception.getMessage(),
                details,
                request.getHeader("X-Trace-Id"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(response);
    }
}
