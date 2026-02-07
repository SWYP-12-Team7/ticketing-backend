package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.Curation;
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
    public static ExhibitionSummary from(Curation curation, boolean isLiked) {
        return ExhibitionSummary.builder()
            .exhibitionId(curation.getId())
            .title(curation.getTitle())
            .subTitle(curation.getSubTitle())
            .thumbnail(curation.getThumbnail())
            .region(curation.getRegion())
            .place(curation.getPlace())
            .startDate(curation.getStartDate())
            .endDate(curation.getEndDate())
            .tags(curation.getTags())
            .likeCount(curation.getLikeCount())
            .viewCount(curation.getViewCount())
            .isLiked(isLiked)
            .build();
    }
}
