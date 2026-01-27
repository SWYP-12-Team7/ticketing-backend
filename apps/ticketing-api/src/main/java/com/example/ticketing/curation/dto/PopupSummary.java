package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.CurationStatus;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.ReservationStatus;
import java.time.LocalDate;
import java.util.List;

public record PopupSummary(
    Long id,
    String title,
    String thumbnail,
    LocalDate startDate,
    LocalDate endDate,
    CurationStatus status,
    LocationSummary location,
    List<String> category,
    boolean isFree,
    ReservationStatus reservationStatus,
    List<String> tags,
    boolean isLiked
) {
    public static PopupSummary from(Popup popup, boolean isLiked) {
        return new PopupSummary(
            popup.getId(),
            popup.getTitle(),
            popup.getThumbnail(),
            popup.getStartDate(),
            popup.getEndDate(),
            popup.calculateStatus(),
            new LocationSummary(
                popup.getRegion(),
                popup.getPlace(),
                popup.getAddress()
            ),
            popup.getCategory(),
            popup.isFree(),
            popup.getReservationStatus(),
            popup.getTags(),
            isLiked
        );
    }
}
