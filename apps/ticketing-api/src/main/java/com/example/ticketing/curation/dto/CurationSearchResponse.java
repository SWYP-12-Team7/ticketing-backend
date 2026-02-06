package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;

import java.time.LocalDate;
import java.util.List;

public record CurationSearchResponse(
        List<CurationItem> curations,
        Pagination pagination
) {
    public record CurationItem(
            Long id,
            String title,
            String thumbnail,
            CurationType type,
            List<String> tags,
            String location,
            String period
    ) {
        public static CurationItem from(Curation c) {
            String period = formatPeriod(c.getStartDate(), c.getEndDate());
            return new CurationItem(
                    c.getId(),
                    c.getTitle(),
                    c.getThumbnail(),
                    c.getType(),
                    c.getTags(),
                    c.getRegion(),
                    period
            );
        }

        private static String formatPeriod(LocalDate startDate, LocalDate endDate) {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate == null) {
                return "~ " + endDate;
            }
            if (endDate == null) {
                return startDate + " ~";
            }
            return startDate + " ~ " + endDate;
        }
    }

    public record Pagination(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
