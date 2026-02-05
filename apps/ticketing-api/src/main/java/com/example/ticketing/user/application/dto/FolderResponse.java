package com.example.ticketing.user.application.dto;

import com.example.ticketing.user.domain.FavoriteFolder;

public record FolderResponse(
        Long id,
        String name,
        long totalCount,
        long popupCount,
        long exhibitionCount
) {
    public static FolderResponse from(FavoriteFolder folder, long popupCount, long exhibitionCount) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                popupCount + exhibitionCount,
                popupCount,
                exhibitionCount
        );
    }
}