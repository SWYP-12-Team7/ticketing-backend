package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.CurationStatus;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PopupDetailResponse(
    Long id,
    String title,
    String subTitle,
    String thumbnail,
    String image,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    CurationStatus status,
    LocationSummary location,
    List<String> category,
    boolean isFree,
    ReservationStatus reservationStatus,
    List<String> tags,
    String url,
    String description,
    boolean isLiked
) {
    public static PopupDetailResponse from(Popup popup, boolean isLiked) {
        return new PopupDetailResponse(
            popup.getId(),
            popup.getTitle(),
            popup.getSubTitle(),
            popup.getThumbnail(),
            popup.getImage(),
            popup.getStartDate(),
            popup.getEndDate(),
            popup.getStartTime(),
            popup.getEndTime(),
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
            popup.getUrl(),
            popup.getDescription(),
            isLiked
        );
    }
}
