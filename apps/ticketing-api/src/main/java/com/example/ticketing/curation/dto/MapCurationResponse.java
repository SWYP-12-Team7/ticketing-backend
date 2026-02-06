package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record MapCurationResponse(
        List<MapCurationItem> items
) {
    public record MapCurationItem(
            Long id,
            CurationType type,
            String title,
            String thumbnail,
            List<String> category,
            Long likeCount,
            Long viewCount,
            String dateText,
            Double latitude,
            Double longitude
    ) {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        public static MapCurationItem from(Curation curation) {
            String dateText = formatDateRange(curation);
            return new MapCurationItem(
                    curation.getId(),
                    curation.getType(),
                    curation.getTitle(),
                    curation.getThumbnail(),
                    curation.getCategory(),
                    curation.getLikeCount(),
                    curation.getViewCount(),
                    dateText,
                    curation.getLatitude(),
                    curation.getLongitude()
            );
        }

        private static String formatDateRange(Curation curation) {
            if (curation.getStartDate() == null) return "";
            String start = curation.getStartDate().format(DATE_FORMATTER);
            if (curation.getEndDate() == null) return start;
            String end = curation.getEndDate().format(DATE_FORMATTER);
            return start + " - " + end;
        }
    }
}