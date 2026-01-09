package com.example.ticketing.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    int status,
    String code,
    String message,
    LocalDateTime timestamp
) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, null, message, LocalDateTime.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
            errorCode.getStatus().value(),
            errorCode.name(),
            message,
            LocalDateTime.now()
        );
    }
}
