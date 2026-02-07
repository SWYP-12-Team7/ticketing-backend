package com.example.ticketing.curation.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.dto.CurationKeywordResponse;
import com.example.ticketing.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurationKeywordService {

    private final CurationRepository curationRepository;

    @Transactional(readOnly = true)
    public CurationKeywordResponse getRecommendedKeywords() {
        LocalDate today = LocalDate.now();
        LocalDate sunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate saturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        List<Curation> recentCurations = curationRepository.findRecentWithTags(sunday, saturday);

        List<String> keywords = recentCurations.stream()
                .map(Curation::getTags)
                .filter(tags -> tags != null && !tags.isEmpty())
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        return new CurationKeywordResponse(keywords);
    }
}
