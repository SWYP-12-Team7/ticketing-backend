package com.example.ticketing.curation.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(
        int year,
        int month,
        List<DayCount> days
) {
    public record DayCount(
            LocalDate date,
            long popupCount,
            long exhibitionCount
    ) {}
}