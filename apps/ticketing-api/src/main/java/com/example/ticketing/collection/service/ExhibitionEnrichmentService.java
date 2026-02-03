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
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

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

            log.info("Open AI 호출 시작");
            String prompt = buildPrompt(inputJson);
            log.info("Open AI 호출 종료");

            String response = openAiChatClient.chat(prompt);

            return parseResponse(response);
        } catch (Exception e) {
            log.warn("LLM 배치 보강 실패 - {}건", inputs.size(), e);
            return Collections.emptyList();
        }
    }

    private String buildPrompt(String inputJson) {
        return """
            전시 정보 분석 후 동일 순서로 JSON 배열 반환.

            입력: %s

            출력 형식 (배열 구조 엄수):
            [{"category":["카테고리"],"tags":["태그1","태그2","태그3"],"startTime":"HH:mm","endTime":"HH:mm"}]

            규칙:
            - category: 반드시 배열로 반환. %s 중 1개만 선택해서 ["선택값"] 형식
            - tags: 배열로 3-5개 (예: ["무료관람","사진전","체험형"])
            - startTime/endTime: eventPeriod에서 추출, 없으면 null

            순수 JSON만 반환.
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
