package com.example.ticketing.mainpage.dto;

import java.time.LocalDate;
import java.util.List;

public record PopularCurationResponse(
        PopularByPeriod popup,
        PopularByPeriod exhibition
) {
    public record PopularByPeriod(
            List<PopularItem> daily,
            List<PopularItem> weekly,
            List<PopularItem> monthly
    ) {}

    public record PopularItem(
            int rank,
            Long id,
            String title,
            String thumbnail,
            String address,
            String period
    ) {
        public static PopularItem of(int rank, Long id, String title, String thumbnail, String region, LocalDate startDate, LocalDate endDate) {
            String period = formatPeriod(startDate, endDate);
            return new PopularItem(rank, id, title, thumbnail, region, period);
        }

        private static String formatPeriod(LocalDate startDate, LocalDate endDate) {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate == null) {
                return "~ " + endDate.toString();
            }
            if (endDate == null) {
                return startDate.toString() + " ~";
            }
            return startDate.toString() + " ~ " + endDate.toString();
        }
    }
}
