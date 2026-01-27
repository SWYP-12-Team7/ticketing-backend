package com.example.ticketing.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    String result,
    T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", data);
    }

    public static <T> ApiResponse<T> error(T data) {
        return new ApiResponse<>("ERROR", data);
    }
}
