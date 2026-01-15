package com.example.ticketing.curation.dto;

import java.util.List;

public record PopupListResponse(
    List<PopupSummary> popups,
    UserContext userContext,
    Pagination pagination
) {
    public static PopupListResponse of(
        List<PopupSummary> popups,
        List<String> likedPopupIds,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
        return new PopupListResponse(
            popups,
            likedPopupIds != null ? new UserContext(likedPopupIds) : null,
            new Pagination(page, size, totalElements, totalPages)
        );
    }
}