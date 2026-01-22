package com.example.ticketing.collection.service;

import com.example.ticketing.collection.dto.GeminiPopupData;
import com.example.ticketing.collection.dto.GeminiPopupResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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

    private String buildPrompt() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        return """
            서울 성수동에서 %d년 %d월에 진행 중이거나 예정된 실제 팝업스토어 정보를 검색해서 알려줘.

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
                  "confidence": 0.85
                }
              ]
            }

            규칙:
            - 실제 브랜드명과 팝업 이름을 사용할 것
            - 실제 장소명(쎈느, 피치스 도원, 성수 에스팩토리, 무신사 테라스 등)을 사용할 것
            - thumbnailImageUrl: 해당 팝업의 공식 홍보 이미지 URL 또는 브랜드 로고 이미지 URL을 찾아서 넣을 것. 찾을 수 없으면 null
            - 무료여부는 Y, N으로 표현
            - 예약필요여부는 Y, N으로 표현
            - 신뢰도(confidence)는 정보의 정확성에 따라 0.00~1.00 사이로 표현
            - 날짜는 yyyy-MM-dd 형식
            - 최소 15개에서 최대 30개까지 알려줘
            """.formatted(year, month);
    }

    public List<GeminiPopupData> collectPopups() {
        log.info("Gemini API를 통해 팝업 데이터 수집 시작");

        try {
            ChatClient chatClient = chatClientBuilder.build();

            String response = chatClient.prompt()
                    .user(buildPrompt())
                    .call()
                    .content();

            log.debug("Gemini 응답: {}", response);

            return parseResponse(response);
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            return Collections.emptyList();
        }
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