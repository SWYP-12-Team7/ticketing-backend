package com.example.ticketing.collection.service;

import com.example.ticketing.collection.config.AiChatClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final AiChatClient openAiChatClient;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final List<String> ALLOWED_CATEGORIES = List.of(
            "현대미술", "사진", "디자인", "일러스트", "회화", "조각", "설치 미술"
    );

    /**
     * 여러 전시 정보를 배치로 분석하여 카테고리, 시간, 태그를 추출
     */
    public List<EnrichmentResult> enrich(List<EnrichmentInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String inputJson = objectMapper.writeValueAsString(inputs);
            String prompt = buildPrompt(inputJson);
            String response = openAiChatClient.chat(prompt);

            return parseResponse(response);
        } catch (Exception e) {
            log.warn("LLM 배치 보강 실패 - {}건", inputs.size(), e);
            return Collections.emptyList();
        }
    }

    private String buildPrompt(String inputJson) {
        return """
            다음 JSON 배열로 전달되는 전시(exhibition) 정보를 분석해서 동일한 순서로 결과를 반환해줘.

            입력 데이터:
            %s

            다음 JSON 배열 형식으로 응답해줘 (입력과 동일한 개수, 동일한 순서):
            [
              {
                "category": ["카테고리"],
                "tags": ["태그1", "태그2", "태그3"],
                "startTime": "HH:mm",
                "endTime": "HH:mm"
              }
            ]

            규칙:
            1. category: 반드시 다음 중 1개만 선택 - %s
               - 제목과 설명을 보고 가장 적합한 카테고리 1개를 선택
               - 정확히 일치하는 단어로만 반환

            2. tags: 3~5개 생성
               - 전시의 특징을 나타내는 키워드
               - 예: "무료관람", "사진전", "현대미술", "팝업", "체험형" 등

            3. startTime, endTime: 운영 시간 추출
               - eventPeriod에서 시간을 찾아서 HH:mm 형식으로 반환
               - 요일별로 다른 경우 가장 일반적인 시간 선택
               - 시간 정보가 없으면 null로 반환

            순수 JSON 배열만 반환하고 다른 설명은 하지 마.
            """.formatted(inputJson, String.join(", ", ALLOWED_CATEGORIES));
    }

    private List<EnrichmentResult> parseResponse(String response) {
        try {
            String json = extractJson(response);
            List<EnrichmentDto> dtos = objectMapper.readValue(json, new TypeReference<>() {});

            return dtos.stream()
                    .map(this::toEnrichmentResult)
                    .toList();
        } catch (Exception e) {
            log.warn("LLM 응답 파싱 실패: {}", response, e);
            return Collections.emptyList();
        }
    }

    private String extractJson(String response) {
        String json = response.trim();
        if (json.startsWith("```json")) json = json.substring(7);
        if (json.startsWith("```")) json = json.substring(3);
        if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
        return json.trim();
    }

    private EnrichmentResult toEnrichmentResult(EnrichmentDto dto) {
        List<String> validCategories = dto.category != null
                ? dto.category.stream()
                .filter(ALLOWED_CATEGORIES::contains)
                .limit(1)
                .toList()
                : List.of();

        return new EnrichmentResult(
                validCategories.isEmpty() ? null : validCategories,
                dto.tags != null && !dto.tags.isEmpty() ? dto.tags : null,
                parseTime(dto.startTime),
                parseTime(dto.endTime)
        );
    }

    private LocalDateTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank() || "null".equals(timeStr)) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalDate.now().atTime(
                    java.time.LocalTime.parse(timeStr.trim(), formatter)
            );
        } catch (DateTimeParseException e) {
            log.debug("시간 파싱 실패: {}", timeStr);
            return null;
        }
    }

    public record EnrichmentInput(
            String title,
            String description,
            String eventPeriod
    ) {}

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
