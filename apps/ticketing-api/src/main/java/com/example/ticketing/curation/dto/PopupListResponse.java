package com.example.ticketing.curation.dto;

import java.util.List;

public record PopupListResponse(
    List<PopupSummary> popups,
    Pagination pagination
) {
    public static PopupListResponse of(
        List<PopupSummary> popups,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
        return new PopupListResponse(
            popups,
            new Pagination(page, size, totalElements, totalPages)
        );
    }
}