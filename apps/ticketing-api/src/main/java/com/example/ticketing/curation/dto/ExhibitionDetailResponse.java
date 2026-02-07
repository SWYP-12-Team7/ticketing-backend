package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ExhibitionDetailResponse(
    Long exhibitionId,
    String title,
    String subTitle,
    String thumbnail,
    String region,
    String place,
    String address,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime startTime,
    LocalDateTime endTime,
    List<String> tags,
    String url,
    String description,
    String image,
    ReservationStatus reservationStatus,
    Double latitude,
    Double longitude,
    Long likeCount,
    Long viewCount,
    boolean isLiked
) {
    public static ExhibitionDetailResponse from(Curation curation, boolean isLiked) {
        return ExhibitionDetailResponse.builder()
            .exhibitionId(curation.getId())
            .title(curation.getTitle())
            .subTitle(curation.getSubTitle())
            .thumbnail(curation.getThumbnail())
            .region(curation.getRegion())
            .place(curation.getPlace())
            .address(curation.getAddress())
            .startDate(curation.getStartDate())
            .endDate(curation.getEndDate())
            .startTime(curation.getStartTime())
            .endTime(curation.getEndTime())
            .tags(curation.getTags())
            .url(curation.getUrl())
            .description(curation.getDescription())
            .image(curation.getImage())
            .reservationStatus(curation.getReservationStatus())
            .latitude(curation.getLatitude())
            .longitude(curation.getLongitude())
            .likeCount(curation.getLikeCount())
            .viewCount(curation.getViewCount())
            .isLiked(isLiked)
            .build();
    }
}
