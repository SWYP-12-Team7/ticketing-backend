package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.PopupStatus;
import java.time.LocalDate;
import java.util.List;

public record PopupDetailResponse(
    String popupId,
    String title,
    String thumbnailImageUrl,
    LocalDate startDate,
    LocalDate endDate,
    PopupStatus status,
    LocationSummary location,
    List<String> category,
    boolean isFree,
    boolean reservationRequired,
    List<String> tags,
    UserContext userContext
) {
    public static PopupDetailResponse from(Popup popup, List<String> likedPopupIds) {
        boolean isLiked = likedPopupIds != null && likedPopupIds.contains(popup.getPopupId());

        return new PopupDetailResponse(
            popup.getPopupId(),
            popup.getTitle(),
            popup.getThumbnailImageUrl(),
            popup.getStartDate(),
            popup.getEndDate(),
            popup.calculateStatus(),
            new LocationSummary(
                popup.getCity(),
                popup.getDistrict(),
                popup.getPlaceName()
            ),
            popup.getCategory(),
            popup.isFree(),
            popup.isReservationRequired(),
            popup.getTags(),
            likedPopupIds != null ? new UserContext(likedPopupIds) : null
        );
    }
}
