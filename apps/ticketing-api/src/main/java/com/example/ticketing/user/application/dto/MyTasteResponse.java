package com.example.ticketing.user.application.dto;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;

import java.time.LocalDate;
import java.util.List;

public record MyTasteResponse(
        List<CurationSummary> favorites,
        List<CurationSummary> recentViews,
        List<CurationSummary> recommendations
) {
    public record CurationSummary(
            Long id,
            CurationType type,
            String title,
            String thumbnail,
            String region,
            String place,
            LocalDate startDate,
            LocalDate endDate
    ) {
        public static CurationSummary from(Curation curation) {
            return new CurationSummary(
                    curation.getId(),
                    curation.getType(),
                    curation.getTitle(),
                    curation.getThumbnail(),
                    curation.getRegion(),
                    curation.getPlace(),
                    curation.getStartDate(),
                    curation.getEndDate()
            );
        }
    }
}