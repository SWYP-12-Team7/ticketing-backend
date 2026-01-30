package com.example.ticketing.user.application.dto;

public record OnboardingCategoryResponse(
        String category,
        String thumbnailUrl,
        String type
) {
}