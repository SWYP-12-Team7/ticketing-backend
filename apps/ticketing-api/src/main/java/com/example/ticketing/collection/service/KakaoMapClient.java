package com.example.ticketing.collection.service;

import com.example.ticketing.common.config.KakaoOAuthProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


/**
 * 카카오 지도 API 클라이언트
 * - 키워드로 장소 검색하여 위경도 및 지역 정보 반환
 */
@Slf4j
@Component
public class KakaoMapClient {

    private static final String KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    private final RestClient restClient;
    private final KakaoOAuthProperties properties;

    public KakaoMapClient(KakaoOAuthProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    /**
     * 키워드로 장소를 검색하여 위치 정보 반환
     * @param keyword 검색 키워드 (장소명)
     * @return 위치 정보 (위도, 경도, 지역)
     */
    public Optional<LocationInfo> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Optional.empty();
        }

        try {
            String responseBody = restClient.get()
                    .uri(KEYWORD_SEARCH_URL + "?query={query}", keyword)
                    .header("Authorization", "KakaoAK " + properties.clientId())
                    .retrieve()
                    .body(String.class);

            KakaoSearchResponse response = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(responseBody, KakaoSearchResponse.class);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                log.info("카카오 지도 검색 결과 없음 - keyword: {}", keyword);
                return Optional.empty();
            }

            KakaoDocument doc = response.documents().get(0);
            String region = extractRegion(doc.addressName());

            return Optional.of(new LocationInfo(
                    Double.parseDouble(doc.y()),  // latitude
                    Double.parseDouble(doc.x()),  // longitude
                    region,
                    doc.roadAddressName()
            ));
        } catch (Exception e) {
            log.warn("카카오 지도 API 호출 실패 - keyword: {}", keyword, e);
            return Optional.empty();
        }
    }

    /**
     * 주소에서 시/도 단위 지역 추출
     * @param addressName 지번 주소 (예: "서울 강남구 삼성동 159")
     * @return 시/도 단위 지역 (예: "서울")
     */
    private String extractRegion(String addressName) {
        if (addressName == null || addressName.isBlank()) {
            return null;
        }
        String[] parts = addressName.split("\\s+");
        return parts.length > 0 ? parts[0] : null;
    }

    public record LocationInfo(
            Double latitude,
            Double longitude,
            String region,
            String address
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoSearchResponse(
            List<KakaoDocument> documents
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoDocument(
            String x,
            String y,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            @JsonProperty("place_name") String placeName
    ) {}
}
