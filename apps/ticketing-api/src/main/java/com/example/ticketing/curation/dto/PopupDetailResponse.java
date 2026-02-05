package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.PopupStatus;
import com.example.ticketing.curation.domain.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record PopupDetailResponse(
    // Curation 필드 (부모)
    Long id,
    CurationType type,
    String title,
    String subTitle,
    String thumbnail,
    String region,
    String place,
    LocalDate startDate,
    LocalDate endDate,
    List<String> category,
    List<String> tags,
    String url,
    String address,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String description,
    String image,
    ReservationStatus reservationStatus,
    Long likeCount,
    Long viewCount,
    Double latitude,
    Double longitude,
    // Popup 필드 (자식 고유)
    String popupId,
    String city,
    String district,
    String placeName,
    boolean isFree,
    String homepageUrl,
    String snsUrl,
    Map<String, String> operatingHours,
    // 계산된 필드
    PopupStatus status
) {
    public static PopupDetailResponse from(Popup popup) {
        return new PopupDetailResponse(
            // Curation 필드 (부모)
            popup.getId(),
            popup.getType(),
            popup.getTitle(),
            popup.getSubTitle(),
            popup.getThumbnail(),
            popup.getRegion(),
            popup.getPlace(),
            popup.getStartDate(),
            popup.getEndDate(),
            popup.getCategory(),
            popup.getTags(),
            popup.getUrl(),
            popup.getAddress(),
            popup.getStartTime(),
            popup.getEndTime(),
            popup.getDescription(),
            popup.getImage(),
            popup.getReservationStatus(),
            popup.getLikeCount(),
            popup.getViewCount(),
            popup.getLatitude(),
            popup.getLongitude(),
            // Popup 필드 (자식 고유)
            popup.getPopupId(),
            popup.getCity(),
            popup.getDistrict(),
            popup.getPlaceName(),
            popup.isFree(),
            popup.getHomepageUrl(),
            popup.getSnsUrl(),
            popup.getOperatingHours(),
            // 계산된 필드
            popup.calculateStatus()
        );
    }
}
