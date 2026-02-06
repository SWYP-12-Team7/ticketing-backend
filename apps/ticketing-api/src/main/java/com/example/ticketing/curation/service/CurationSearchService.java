package com.example.ticketing.curation.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.dto.CurationSearchResponse;
import com.example.ticketing.curation.dto.CurationSearchResponse.CurationItem;
import com.example.ticketing.curation.dto.CurationSearchResponse.Pagination;
import com.example.ticketing.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurationSearchService {

    private final CurationRepository curationRepository;

    public CurationSearchResponse search(String keyword, CurationType type, String category, int page, int size) {
        String typeStr = type != null ? type.name() : null;
        int offset = page * size;

        // 1. ID 목록 조회 (native query)
        List<Long> curationIds = curationRepository.searchCurationIds(keyword, typeStr, category, size, offset);
        long totalElements = curationRepository.countSearchCurations(keyword, typeStr, category);

        // 2. ID로 엔티티 조회 (JPA - 상속 관계 정상 처리)
        List<CurationItem> items;
        if (curationIds.isEmpty()) {
            items = Collections.emptyList();
        } else {
            List<Curation> curations = curationRepository.findAllById(curationIds);

            // 원래 순서 유지
            Map<Long, Curation> curationMap = curations.stream()
                    .collect(Collectors.toMap(Curation::getId, c -> c));

            items = curationIds.stream()
                    .map(curationMap::get)
                    .filter(c -> c != null)
                    .map(CurationItem::from)
                    .toList();
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);
        Pagination pagination = new Pagination(page, size, totalElements, totalPages);

        return new CurationSearchResponse(items, pagination);
    }
}
