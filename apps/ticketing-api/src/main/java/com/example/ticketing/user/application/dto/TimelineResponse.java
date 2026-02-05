package com.example.ticketing.user.application.dto;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record TimelineResponse(
        List<TimelineItem> upcoming,    // 오픈 예정
        List<TimelineItem> ongoing      // 진행중
) {
    public record TimelineItem(
            Long curationId,
            CurationType curationType,
            String thumbnail,
            String title,
            String description,
            List<String> tags,
            String place,
            String dateText,        // "2026.01.03 - 2026.01.31" 형식
            LocalDate startDate,
            LocalDate endDate,
            Long viewCount,
            Long likeCount
    ) {
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        public static TimelineItem from(Curation curation) {
            String dateText = formatDateRange(curation.getStartDate(), curation.getEndDate());

            return new TimelineItem(
                    curation.getId(),
                    curation.getType(),
                    curation.getThumbnail(),
                    curation.getTitle(),
                    curation.getDescription(),
                    curation.getTags(),
                    curation.getPlace(),
                    dateText,
                    curation.getStartDate(),
                    curation.getEndDate(),
                    curation.getViewCount(),
                    curation.getLikeCount()
            );
        }

        private static String formatDateRange(LocalDate start, LocalDate end) {
            if (start == null && end == null) return "";
            if (start == null) return "~ " + end.format(DATE_FORMAT);
            if (end == null) return start.format(DATE_FORMAT) + " ~";
            return start.format(DATE_FORMAT) + " - " + end.format(DATE_FORMAT);
        }
    }
}