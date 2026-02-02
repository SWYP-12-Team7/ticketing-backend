package com.example.ticketing.collection.service;

import com.example.ticketing.curation.domain.Exhibition;
import com.example.ticketing.curation.repository.ExhibitionRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExhibitionTagEnricher {

    private final ChatClient.Builder chatClientBuilder;
    private final ExhibitionRepository exhibitionRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // TODO: Curation에 updateTags/updateCategory 추가 후 활성화
    // public int enrichAll() {
    //     List<Exhibition> exhibitions = exhibitionRepository.findByTagsIsNullOrCategoryIsNull();
    //     log.info("태그/카테고리 보강 대상: {}건", exhibitions.size());
    //
    //     ChatClient chatClient = chatClientBuilder.build();
    //     int enrichedCount = 0;
    //
    //     for (Exhibition exhibition : exhibitions) {
    //         try {
    //             String prompt = buildPrompt(exhibition);
    //             String response = chatClient.prompt().user(prompt).call().content();
    //             EnrichResult result = parseResponse(response);
    //
    //             if (result != null) {
    //                 exhibition.enrichTagsAndCategory(result.tags(), result.category());
    //                 exhibitionRepository.save(exhibition);
    //                 enrichedCount++;
    //                 log.debug("[보강] {} - tags: {}, category: {}", exhibition.getTitle(), result.tags(), result.category());
    //             }
    //         } catch (Exception e) {
    //             log.warn("태그 보강 실패: {}", exhibition.getTitle(), e);
    //         }
    //     }
    //
    //     log.info("태그/카테고리 보강 완료: {}건", enrichedCount);
    //     return enrichedCount;
    // }

    private String buildPrompt(Exhibition exhibition) {
        return """
            다음 전시 정보를 보고 적절한 tags와 category를 추천해줘.

            제목: %s
            설명: %s
            장소: %s

            다음 JSON 형식으로만 응답해줘:
            {
              "tags": ["태그1", "태그2", "태그3"],
              "category": ["카테고리1"]
            }

            규칙:
            - tags는 3~5개 (예: "현대미술", "무료", "가족", "체험", "사진")
            - category는 1~2개 (예: "미술", "사진", "디자인", "공예", "역사", "과학", "복합")
            - 순수 JSON만 반환
            """.formatted(
                exhibition.getTitle(),
                exhibition.getDescription() != null ? exhibition.getDescription() : "없음",
                exhibition.getPlace() != null ? exhibition.getPlace() : "없음"
        );
    }

    private EnrichResult parseResponse(String response) {
        try {
            String json = response.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            return objectMapper.readValue(json.trim(), EnrichResult.class);
        } catch (Exception e) {
            log.warn("GPT 응답 파싱 실패: {}", response, e);
            return null;
        }
    }

    public record EnrichResult(List<String> tags, List<String> category) {}
}