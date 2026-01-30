package com.example.ticketing.collection.service;

import com.example.ticketing.collection.dto.GeminiPopupData;
import com.example.ticketing.collection.dto.GeminiPopupResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiPopupCollector {

    private final ChatClient.Builder chatClientBuilder;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final List<String> TARGET_LOCATIONS = List.of(
            "서울 성수동",
            "서울 홍대",
            "서울 강남",
            "서울 잠실",
            "서울 여의도"
    );

    private static final long DELAY_BETWEEN_REQUESTS_SECONDS = 60;

    private String buildPrompt(String location) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        return """
            %s에서 %d년 %d월에 진행 중이거나 예정된 실제 팝업스토어 정보를 검색해서 알려줘.

            중요: 가상의 데이터가 아닌 실제로 존재하는 팝업스토어 정보만 알려줘.
            브랜드명, 장소명, 날짜 등 정확한 정보를 포함해줘.

            다음 JSON 형식으로만 응답해줘. 다른 텍스트 없이 순수 JSON만 반환해줘:
            {
              "popups": [
                {
                  "title": "팝업 제목 (브랜드명 포함)",
                  "thumbnailImageUrl": "https://example.com/image.jpg",
                  "startDate": "2026-01-01",
                  "endDate": "2026-01-31",
                  "city": "서울",
                  "district": "성동구",
                  "placeName": "실제 장소명",
                  "categories": ["카테고리1", "카테고리2"],
                  "isFree": "Y",
                  "reservationRequired": "N",
                  "tags": ["태그1", "태그2"],
                  "confidence": 0.85,
                  "homepageUrl": "https://brand.com/popup",
                  "snsUrl": "https://instagram.com/brand"
                }
              ]
            }

            규칙:
            - 실제 브랜드명과 팝업 이름을 사용할 것
            - 실제 장소명(쎈느, 피치스 도원, 성수 에스팩토리, 무신사 테라스 등)을 사용할 것
            - thumbnailImageUrl: 해당 팝업의 공식 홍보 이미지 URL 또는 브랜드 로고 이미지 URL을 찾아서 넣을 것. 찾을 수 없으면 null
            - homepageUrl: 해당 팝업 또는 브랜드의 공식 홈페이지 URL. 찾을 수 없으면 null
            - snsUrl: 해당 팝업 또는 브랜드의 공식 SNS URL (인스타그램, 트위터 등). 찾을 수 없으면 null
            - 무료여부는 Y, N으로 표현
            - 예약필요여부는 Y, N으로 표현
            - 신뢰도(confidence)는 정보의 정확성에 따라 0.00~1.00 사이로 표현
            - 날짜는 yyyy-MM-dd 형식
            - 최소 15개에서 최대 30개까지 알려줘
            """.formatted(location, year, month);
    }

    public List<GeminiPopupData> collectPopups() {
        log.info("Gemini API를 통해 팝업 데이터 수집 시작 - 대상 지역: {}", TARGET_LOCATIONS);

        List<GeminiPopupData> allPopups = new ArrayList<>();
        ChatClient chatClient = chatClientBuilder.build();

        for (int i = 0; i < TARGET_LOCATIONS.size(); i++) {
            String location = TARGET_LOCATIONS.get(i);

            // Rate Limit 방지: 첫 요청 이후 60초 딜레이
            if (i > 0) {
                try {
                    log.info("Rate Limit 방지를 위해 {}초 대기 중...", DELAY_BETWEEN_REQUESTS_SECONDS);
                    TimeUnit.SECONDS.sleep(DELAY_BETWEEN_REQUESTS_SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("대기 중 인터럽트 발생");
                }
            }

            try {
                log.info("'{}' 지역 팝업 데이터 수집 중... ({}/{})", location, i + 1, TARGET_LOCATIONS.size());

                String response = chatClient.prompt()
                        .user(buildPrompt(location))
                        .call()
                        .content();

                log.debug("Gemini 응답 ({}): {}", location, response);

                List<GeminiPopupData> popups = parseResponse(response);
                log.info("'{}' 지역에서 {}개 팝업 수집 완료", location, popups.size());

                allPopups.addAll(popups);
            } catch (Exception e) {
                log.error("'{}' 지역 Gemini API 호출 중 오류 발생", location, e);
            }
        }

        log.info("전체 팝업 데이터 수집 완료 - 총 {}개", allPopups.size());
        return allPopups;
    }

    private List<GeminiPopupData> parseResponse(String response) {
        try {
            // JSON 블록 추출 (```json ... ``` 형식 처리)
            String jsonContent = extractJsonContent(response);

            GeminiPopupResponse popupResponse = objectMapper.readValue(jsonContent, GeminiPopupResponse.class);
            log.info("파싱된 팝업 수: {}", popupResponse.popups().size());
            return popupResponse.popups();
        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 파싱 실패: {}", response, e);
            return Collections.emptyList();
        }
    }

    private String extractJsonContent(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }

        // ```json 또는 ``` 블록 제거
        String trimmed = response.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        return trimmed.trim();
    }
}