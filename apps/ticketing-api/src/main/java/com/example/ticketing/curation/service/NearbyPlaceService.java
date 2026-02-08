package com.example.ticketing.curation.service;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.dto.NearbyPlaceResponse;
import com.example.ticketing.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NearbyPlaceService {

    private final CurationRepository curationRepository;
    private final KakaoPlaceClient kakaoPlaceClient;

    private static final String CATEGORY_RESTAURANT = "FD6";  // 음식점
    private static final String CATEGORY_CAFE = "CE7";        // 카페
    private static final int DEFAULT_RADIUS = 1000;           // 1km

    /**
     * 행사 주변 인기 식당/카페 조회
     */
    public NearbyPlaceResponse getNearbyPlaces(Long curationId) {
        Curation curation = curationRepository.findById(curationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURATION_NOT_FOUND));

        Double latitude = curation.getLatitude();
        Double longitude = curation.getLongitude();

        if (latitude == null || longitude == null) {
            return new NearbyPlaceResponse(Collections.emptyList(), Collections.emptyList());
        }

        List<NearbyPlaceResponse.PlaceItem> restaurants = kakaoPlaceClient.searchNearbyPlaces(
                latitude, longitude, DEFAULT_RADIUS, CATEGORY_RESTAURANT
        );

        List<NearbyPlaceResponse.PlaceItem> cafes = kakaoPlaceClient.searchNearbyPlaces(
                latitude, longitude, DEFAULT_RADIUS, CATEGORY_CAFE
        );

        return new NearbyPlaceResponse(restaurants, cafes);
    }
}