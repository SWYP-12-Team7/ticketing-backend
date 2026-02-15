package com.example.ticketing.user.application.dto;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record FavoriteListResponse(
        List<FavoriteItem> items,
        int currentPage,
        int totalPages,
        long totalElements
) {
    public static FavoriteListResponse empty() {
        return new FavoriteListResponse(Collections.emptyList(), 0, 0, 0);
    }

    public record FavoriteItem(
            Long id,
            Long curationId,
            CurationType curationType,
            String title,
            String thumbnail,
            String region,
            LocalDate startDate,
            LocalDate endDate,
            Long folderId
    ) {
        public static FavoriteItem from(Curation curation, Long favoriteId, Long folderId) {
            return new FavoriteItem(
                    favoriteId,        // UserFavorite의 ID (찜 항목 ID)
                    curation.getId(),  // Curation의 ID (행사 ID)
                    curation.getType(),
                    curation.getTitle(),
                    curation.getThumbnail(),
                    curation.getRegion(),
                    curation.getStartDate(),
                    curation.getEndDate(),
                    folderId
            );
        }
    }
}