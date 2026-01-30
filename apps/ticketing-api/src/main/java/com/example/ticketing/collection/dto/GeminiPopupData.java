package com.example.ticketing.collection.dto;

import java.util.List;

/**
 * Gemini API에서 수집한 팝업 데이터를 담는 DTO
 */
public record GeminiPopupData(
        String title,
        String thumbnailImageUrl,
        String startDate,
        String endDate,
        String city,
        String district,
        String placeName,
        List<String> categories,
        String isFree,
        String reservationRequired,
        List<String> tags,
        Double confidence
) {
    public boolean isFreeBoolean() {
        return "Y".equalsIgnoreCase(isFree);
    }

    public boolean isReservationRequiredBoolean() {
        return "Y".equalsIgnoreCase(reservationRequired);
    }
}
