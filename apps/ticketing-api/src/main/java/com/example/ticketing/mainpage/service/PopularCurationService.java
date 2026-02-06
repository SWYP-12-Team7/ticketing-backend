package com.example.ticketing.mainpage.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.curation.repository.CurationViewHistoryRepository;
import com.example.ticketing.mainpage.dto.PopularCurationResponse;
import com.example.ticketing.mainpage.dto.PopularCurationResponse.PopularItem;
import com.example.ticketing.mainpage.dto.PopularCurationResponse.PopularByPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularCurationService {

    private final CurationViewHistoryRepository viewHistoryRepository;
    private final CurationRepository curationRepository;

    private static final int DEFAULT_LIMIT = 10;

    public PopularCurationResponse getPopularCurations() {
        return getPopularCurations(DEFAULT_LIMIT);
    }

    public PopularCurationResponse getPopularCurations(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dailySince = now.minusHours(24);
        LocalDateTime weeklySince = now.truncatedTo(ChronoUnit.DAYS)
                .with(java.time.DayOfWeek.MONDAY);
        LocalDateTime monthlySince = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        PopularByPeriod popup = new PopularByPeriod(
                getPopularByType(CurationType.POPUP, dailySince, limit),
                getPopularByType(CurationType.POPUP, weeklySince, limit),
                getPopularByType(CurationType.POPUP, monthlySince, limit)
        );

        PopularByPeriod exhibition = new PopularByPeriod(
                getPopularByType(CurationType.EXHIBITION, dailySince, limit),
                getPopularByType(CurationType.EXHIBITION, weeklySince, limit),
                getPopularByType(CurationType.EXHIBITION, monthlySince, limit)
        );

        return new PopularCurationResponse(popup, exhibition);
    }

    private List<PopularItem> getPopularByType(CurationType type, LocalDateTime since, int limit) {
        List<Object[]> results = viewHistoryRepository.findPopularCurationIds(type, since, limit);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> curationIds = results.stream()
                .map(row -> (Long) row[0])
                .toList();

        List<Curation> curations = curationRepository.findAllById(curationIds);

        Map<Long, Curation> curationMap = curations.stream()
                .collect(Collectors.toMap(Curation::getId, c -> c));

        List<PopularItem> items = new ArrayList<>();
        int rank = 1;
        for (Long id : curationIds) {
            Curation c = curationMap.get(id);
            if (c != null) {
                items.add(PopularItem.of(
                        rank++,
                        c.getId(),
                        c.getTitle(),
                        c.getThumbnail(),
                        c.getRegion(),
                        c.getStartDate(),
                        c.getEndDate()
                ));
            }
        }
        return items;
    }
}
