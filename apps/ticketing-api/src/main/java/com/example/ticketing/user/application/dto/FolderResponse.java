package com.example.ticketing.user.application.dto;

import com.example.ticketing.user.domain.FavoriteFolder;

import java.util.List;

public record FolderResponse(
        Long id,
        String name,
        String color,
        long totalCount,
        long popupCount,
        long exhibitionCount,
        List<String> thumbnails
) {
    public static FolderResponse from(FavoriteFolder folder, long popupCount, long exhibitionCount, List<String> thumbnails) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getColor(),
                popupCount + exhibitionCount,
                popupCount,
                exhibitionCount,
                thumbnails
        );
    }
}