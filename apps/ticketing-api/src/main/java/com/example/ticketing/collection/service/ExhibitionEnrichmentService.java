package com.example.ticketing.collection.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * LLM을 활용하여 전시 데이터를 보강하는 서비스
 * - 카테고리 자동 분류
 * - 시간 정보 추출
 * - 태그 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExhibitionEnrichmentService {

    private final ChatClient exhibitionChatClient;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final List<String> ALLOWED_CATEGORIES = List.of(
            "현대미술", "사진", "디자인", "일러스트", "회화", "조각", "설치 미술"
    );

    /**
     * 전시 정보를 분석하여 카테고리, 시간, 태그를 추출
     */
    public EnrichmentResult enrich(String title, String description, String eventPeriod) {
        try {
            String prompt = buildPrompt(title, description, eventPeriod);
            String response = exhibitionChatClient.prompt().user(prompt).call().content();

            return parseResponse(response);
        } catch (Exception e) {
            log.warn("LLM 보강 실패 - title: {}", title, e);
            return null;
        }
    }

    private String buildPrompt(String title, String description, String eventPeriod) {
        return """
            다음 전시 정보를 분석해서 JSON으로 응답해줘.

            제목: %s
            설명: %s
            기간 정보: %s

            다음 JSON 형식으로만 응답해줘:
            {
              "category": ["카테고리"],
              "tags": ["태그1", "태그2", "태그3"],
              "startTime": "HH:mm",
              "endTime": "HH:mm"
            }

            규칙:
            1. category: 반드시 다음 중 1개만 선택 - %s
               - 제목과 설명을 보고 가장 적합한 카테고리 1개를 선택
               - 정확히 일치하는 단어로만 반환

            2. tags: 3~5개 생성
               - 전시의 특징을 나타내는 키워드
               - 예: "무료관람", "사진전", "현대미술", "팝업", "체험형" 등

            3. startTime, endTime: 운영 시간 추출
               - 기간 정보에서 시간을 찾아서 HH:mm 형식으로 반환
               - 요일별로 다른 경우 가장 일반적인 시간 선택
               - 시간 정보가 없으면 null로 반환
               - 예시: "11:00-20:00" → startTime: "11:00", endTime: "20:00"

            순수 JSON만 반환하고 다른 설명은 하지 마.
            """.formatted(
                title,
                description != null ? description : "없음",
                eventPeriod != null ? eventPeriod : "없음",
                String.join(", ", ALLOWED_CATEGORIES)
        );
    }

    private EnrichmentResult parseResponse(String response) {
        try {
            // 마크다운 코드 블록 제거
            String json = response.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.trim();

            EnrichmentDto dto = objectMapper.readValue(json, EnrichmentDto.class);

            // 유효성 검증
            List<String> validCategories = dto.category != null
                    ? dto.category.stream()
                    .filter(ALLOWED_CATEGORIES::contains)
                    .limit(1)  // 1개만 사용
                    .toList()
                    : List.of();

            LocalDateTime startTime = parseTime(dto.startTime);
            LocalDateTime endTime = parseTime(dto.endTime);

            return new EnrichmentResult(
                    validCategories.isEmpty() ? null : validCategories,
                    dto.tags != null && !dto.tags.isEmpty() ? dto.tags : null,
                    startTime,
                    endTime
            );

        } catch (Exception e) {
            log.warn("LLM 응답 파싱 실패: {}", response, e);
            return null;
        }
    }

    private LocalDateTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }

        try {
            // HH:mm 형식을 오늘 날짜의 시간으로 변환
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalDate.now().atTime(
                    java.time.LocalTime.parse(timeStr.trim(), formatter)
            );
        } catch (DateTimeParseException e) {
            log.debug("시간 파싱 실패: {}", timeStr);
            return null;
        }
    }

    private record EnrichmentDto(
            @JsonProperty("category") List<String> category,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("startTime") String startTime,
            @JsonProperty("endTime") String endTime
    ) {}

    public record EnrichmentResult(
            List<String> category,
            List<String> tags,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {}
}