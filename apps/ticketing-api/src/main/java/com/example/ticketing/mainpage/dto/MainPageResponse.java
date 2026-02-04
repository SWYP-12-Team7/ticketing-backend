package com.example.ticketing.mainpage.dto;

import java.time.LocalDate;
import java.util.List;

public record MainPageResponse(
        List<CurationSummary> userCurations,
        List<CurationSummary> upcomingCurations,
        List<CurationSummary> freeCurations,
        List<CurationSummary> todayOpenCurations
) {
    public record CurationSummary(
            Long id,
            String type,
            String title,
            String subTitle,
            String thumbnail,
            String region,
            String place,
            LocalDate startDate,
            LocalDate endDate,
            List<String> category,
            Long dDay
    ) {
        public static CurationSummary of(Long id, String type, String title, String subTitle,
                                         String thumbnail, String region, String place,
                                         LocalDate startDate, LocalDate endDate,
                                         List<String> category) {
            Long dDay = null;
            if (startDate != null) {
                dDay = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), startDate);
            }
            return new CurationSummary(id, type, title, subTitle, thumbnail, region, place,
                    startDate, endDate, category, dDay);
        }
    }
}
