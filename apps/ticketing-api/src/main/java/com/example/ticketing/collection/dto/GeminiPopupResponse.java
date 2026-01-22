package com.example.ticketing.collection.dto;

import java.util.List;

/**
 * Gemini API 응답 전체를 담는 DTO
 */
public record GeminiPopupResponse(
        List<GeminiPopupData> popups
) {
}