package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.PopupStatus;
import com.example.ticketing.curation.domain.ReservationStatus;
import java.time.LocalDate;
import java.util.List;

public record PopupSummary(
    String popupId,
    Long id,
    String title,
    String thumbnailImageUrl,
    LocalDate startDate,
    LocalDate endDate,
    PopupStatus status,
    LocationSummary location,
    List<String> category,
    boolean isFree,
    ReservationStatus reservationStatus,
    List<String> tags,
    boolean isLiked
) {
    public static PopupSummary from(Popup popup, boolean isLiked) {
        return new PopupSummary(
            popup.getPopupId(),
            popup.getId(),
            popup.getTitle(),
            popup.getThumbnail(),
            popup.getStartDate(),
            popup.getEndDate(),
            popup.calculateStatus(),
            new LocationSummary(
                popup.getCity(),
                popup.getDistrict(),
                popup.getPlaceName(),
                popup.getLatitude(),
                popup.getLongitude()
            ),
            popup.getCategory(),
            popup.isFree(),
            popup.getReservationStatus(),
            popup.getTags(),
            isLiked
        );
    }
}