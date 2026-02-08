package com.example.ticketing.curation.service;

import com.example.ticketing.curation.dto.NearbyPlaceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KakaoPlaceClient {

    private final RestClient restClient;
    private final String apiKey;

    public KakaoPlaceClient(@Value("${kakao.oauth.client-id}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .build();
    }

    /**
     * 주변 음식점/카페 검색
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 검색 반경 (미터, 최대 20000)
     * @param category FD6(음식점), CE7(카페)
     */
    public List<NearbyPlaceResponse.PlaceItem> searchNearbyPlaces(
            Double latitude,
            Double longitude,
            int radius,
            String category
    ) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", category)
                            .queryParam("x", longitude)  // 경도
                            .queryParam("y", latitude)   // 위도
                            .queryParam("radius", radius)
                            .queryParam("sort", "distance")
                            .queryParam("size", 15)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || !response.containsKey("documents")) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) response.get("documents");

            return documents.stream()
                    .map(doc -> new NearbyPlaceResponse.PlaceItem(
                            (String) doc.get("place_name"),
                            extractSubCategory((String) doc.get("category_name")),
                            (String) doc.get("place_url"),
                            Double.parseDouble((String) doc.get("y")),  // 위도
                            Double.parseDouble((String) doc.get("x")),  // 경도
                            Integer.parseInt((String) doc.get("distance"))
                    ))
                    .toList();

        } catch (Exception e) {
            log.error("카카오 장소 검색 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 카테고리에서 두 번째 상위 카테고리 추출
     * 예: "음식점 > 한식 > 수제비" → "한식"
     * 예: "음식점 > 카페 > 디저트 카페" → "카페"
     */
    private String extractSubCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return "";
        }
        String[] parts = categoryName.split(" > ");
        if (parts.length >= 2) {
            return parts[1];  // 두 번째 카테고리 반환
        }
        return parts[0];  // 하나만 있으면 그대로 반환
    }
}