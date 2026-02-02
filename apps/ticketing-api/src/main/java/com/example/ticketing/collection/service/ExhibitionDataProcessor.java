package com.example.ticketing.collection.service;

import com.example.ticketing.collection.dto.ExhibitionApiResponse.Item;
import com.example.ticketing.curation.domain.Exhibition;
import com.example.ticketing.curation.repository.ExhibitionRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExhibitionDataProcessor {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionEnrichmentService enrichmentService;

    private static final Pattern PERIOD_PATTERN = Pattern.compile(
            "(\\d{4}[.\\-/]?\\d{1,2}[.\\-/]?\\d{1,2})\\s*~\\s*(\\d{4}[.\\-/]?\\d{1,2}[.\\-/]?\\d{1,2})"
    );
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyyMMdd"),     // API 형식: 20260205
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy.M.d"),
            DateTimeFormatter.ofPattern("yyyy-M-d"),
    };

    @Transactional
    public ProcessResult processAndSave(List<Item> items) {
        log.info("전시 데이터 처리 시작 - 총 {}건", items.size());

        int savedCount = 0;
        int skippedCount = 0;
        List<String> skippedReasons = new ArrayList<>();

        for (Item item : items) {
            try {
                if (item.title() == null || item.title().isBlank()) {
                    skippedCount++;
                    skippedReasons.add("제목 없음");
                    continue;
                }

                if (exhibitionRepository.existsByTitle(item.title())) {
                    skippedCount++;
                    skippedReasons.add("중복: " + item.title());
                    continue;
                }

                // eventPeriod 사용 (period는 비어있음)
                LocalDate[] dates = parsePeriod(item.eventPeriod());

                Exhibition exhibition = Exhibition.builder()
                        .title(item.title())
                        .thumbnail(item.imageObject())
                        .region(extractRegion(item.eventSite()))
                        .place(item.eventSite())
                        .startDate(dates[0])
                        .endDate(dates[1])
                        .url(item.url())
                        .description(item.description())
                        .charge(item.charge())
                        .contactPoint(item.contactPoint())
                        .build();

                // LLM을 활용한 데이터 보강 (카테고리, 태그, 시간 정보)
                try {
                    ExhibitionEnrichmentService.EnrichmentResult enrichment =
                            enrichmentService.enrich(item.title(), item.description(), item.eventPeriod());

                    if (enrichment != null) {
                        exhibition.applyEnrichment(
                                enrichment.category(),
                                enrichment.tags(),
                                enrichment.startTime(),
                                enrichment.endTime()
                        );
                        log.debug("[보강] {} - category: {}, tags: {}, time: {}~{}",
                                item.title(),
                                enrichment.category(),
                                enrichment.tags(),
                                enrichment.startTime(),
                                enrichment.endTime()
                        );
                    }
                } catch (Exception e) {
                    log.warn("[보강 실패] {} - 원본 데이터로 저장", item.title(), e);
                }

                exhibitionRepository.save(exhibition);
                savedCount++;
                log.debug("[저장] {}", item.title());

            } catch (Exception e) {
                skippedCount++;
                skippedReasons.add("처리 오류: " + item.title());
                log.warn("전시 데이터 처리 중 오류: {}", item.title(), e);
            }
        }

        log.info("전시 데이터 처리 완료 - 저장: {}건, 스킵: {}건", savedCount, skippedCount);
        return new ProcessResult(savedCount, skippedCount, skippedReasons);
    }

    private LocalDate[] parsePeriod(String period) {
        if (period == null || period.isBlank()) {
            return new LocalDate[]{null, null};
        }

        Matcher matcher = PERIOD_PATTERN.matcher(period.trim());
        if (matcher.find()) {
            return new LocalDate[]{
                    parseDate(matcher.group(1)),
                    parseDate(matcher.group(2))
            };
        }

        return new LocalDate[]{null, null};
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        log.warn("날짜 파싱 실패: {}", dateStr);
        return null;
    }

    private String extractRegion(String eventSite) {
        if (eventSite == null || eventSite.isBlank()) {
            return null;
        }
        String[] parts = eventSite.split("\\s+");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return parts[0];
    }

    public record ProcessResult(
            int savedCount,
            int skippedCount,
            List<String> skippedReasons
    ) {}
}