package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Exhibition;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ExhibitionSummary(
    Long exhibitionId,
    String title,
    String subTitle,
    String thumbnail,
    String region,
    String place,
    LocalDate startDate,
    LocalDate endDate,
    List<String> tags,
    Long likeCount,
    Long viewCount,
    boolean isLiked
) {
    public static ExhibitionSummary from(Exhibition exhibition, boolean isLiked) {
        return ExhibitionSummary.builder()
            .exhibitionId(exhibition.getId())
            .title(exhibition.getTitle())
            .subTitle(exhibition.getSubTitle())
            .thumbnail(exhibition.getThumbnail())
            .region(exhibition.getRegion())
            .place(exhibition.getPlace())
            .startDate(exhibition.getStartDate())
            .endDate(exhibition.getEndDate())
            .tags(exhibition.getTags())
            .likeCount(exhibition.getLikeCount())
            .viewCount(exhibition.getViewCount())
            .isLiked(isLiked)
            .build();
    }
}
