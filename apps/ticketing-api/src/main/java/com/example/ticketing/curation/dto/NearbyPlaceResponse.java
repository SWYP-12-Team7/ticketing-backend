package com.example.ticketing.curation.dto;

import java.util.List;

public record NearbyPlaceResponse(
    List<PlaceItem> restaurants,
    List<PlaceItem> cafes
) {
    public record PlaceItem(
        String placeName,
        String category,
        String placeUrl,
        Double latitude,
        Double longitude,
        Integer distance  // 미터 단위
    ) {}
}