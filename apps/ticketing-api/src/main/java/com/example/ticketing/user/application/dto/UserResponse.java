package com.example.ticketing.user.application.dto;

public record UserResponse(
        String email,
        String nickname,
        String name,
        String address
) {}
