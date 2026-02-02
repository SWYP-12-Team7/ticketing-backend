package com.example.ticketing.collection.dto;

import com.example.ticketing.curation.domain.ReservationStatus;
import java.util.List;
import java.util.Map;

/**
 * Gemini API에서 수집한 팝업 데이터를 담는 DTO
 */
public record GeminiPopupData(
        String title,
        String subTitle,
        String description,
        String thumbnailImageUrl,
        String startDate,
        String endDate,
        String city,
        String district,
        String placeName,
        String address,
        Map<String, String> operatingHours,
        List<String> categories,
        String isFree,
        String reservationType,
        List<String> tags,
        Double confidence,
        String homepageUrl,
        String snsUrl
) {
    public boolean isFreeBoolean() {
        return "Y".equalsIgnoreCase(isFree);
    }

    /**
     * reservationType을 ReservationStatus로 변환
     * - PRE_ORDER: 사전예약 필요
     * - ON_SITE: 현장 대기
     * - ALL: 둘 다 가능
     */
    public ReservationStatus getReservationStatus() {
        if (reservationType == null) {
            return ReservationStatus.ALL;
        }
        return switch (reservationType.toUpperCase()) {
            case "PRE_ORDER", "RESERVATION", "예약" -> ReservationStatus.PRE_ORDER;
            case "ON_SITE", "WALK_IN", "현장" -> ReservationStatus.ON_SITE;
            default -> ReservationStatus.ALL;
        };
    }
}
