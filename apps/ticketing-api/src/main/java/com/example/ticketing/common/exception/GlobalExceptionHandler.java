package com.example.ticketing.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException: {} - {}", errorCode.name(), e.getMessage());
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    // @Valid + @RequestBody 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null
            ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
            : "입력값이 올바르지 않습니다";
        log.warn("MethodArgumentNotValidException: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
    }

    // @Validated + @PathVariable/@RequestParam 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .orElse("입력값이 올바르지 않습니다");
        log.warn("ConstraintViolationException: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
    }

    // JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "요청 본문을 읽을 수 없습니다"));
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        String message = e.getParameterName() + " 파라미터가 필요합니다";
        log.warn("MissingServletRequestParameterException: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
    }

    // 타입 변환 실패 (예: String -> Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + " 파라미터의 타입이 올바르지 않습니다";
        log.warn("MethodArgumentTypeMismatchException: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다"));
    }
}
