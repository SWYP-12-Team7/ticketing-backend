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
            CurationType type,
            String title,
            String thumbnail,
            String region,
            String place,
            LocalDate startDate,
            LocalDate endDate,
            List<String> category
    ) {
        public static CurationItem from(Curation c) {
            return new CurationItem(
                    c.getId(),
                    c.getType(),
                    c.getTitle(),
                    c.getThumbnail(),
                    c.getRegion(),
                    c.getPlace(),
                    c.getStartDate(),
                    c.getEndDate(),
                    c.getCategory()
            );
        }
    }

    public record Pagination(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
