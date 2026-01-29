package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Exhibition;
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
    Long likeCount,
    Long viewCount,
    boolean isLiked
) {
    public static ExhibitionDetailResponse from(Exhibition exhibition, boolean isLiked) {
        return ExhibitionDetailResponse.builder()
            .exhibitionId(exhibition.getId())
            .title(exhibition.getTitle())
            .subTitle(exhibition.getSubTitle())
            .thumbnail(exhibition.getThumbnail())
            .region(exhibition.getRegion())
            .place(exhibition.getPlace())
            .address(exhibition.getAddress())
            .startDate(exhibition.getStartDate())
            .endDate(exhibition.getEndDate())
            .startTime(exhibition.getStartTime())
            .endTime(exhibition.getEndTime())
            .tags(exhibition.getTags())
            .url(exhibition.getUrl())
            .description(exhibition.getDescription())
            .image(exhibition.getImage())
            .reservationStatus(exhibition.getReservationStatus())
            .likeCount(exhibition.getLikeCount())
            .viewCount(exhibition.getViewCount())
            .isLiked(isLiked)
            .build();
    }
}
