package com.hosteldekho.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now());
        result.put("status", status.value());
        result.put("error", status.getReasonPhrase());
        result.put("message", message);
        return result;
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> api(ApiException exception) {
        return ResponseEntity.status(exception.status).body(body(exception.status, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        Map<String, Object> response = body(HttpStatus.BAD_REQUEST, "Please correct the highlighted fields");
        response.put("fieldErrors", fields);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> constraint(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<?> unreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Request body contains an invalid or missing value"));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<?> badParameter(Exception exception) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "A request parameter or path value is invalid"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "A record with those details already exists"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> other(Exception exception) {
        return ResponseEntity.internalServerError().body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"));
    }
}
