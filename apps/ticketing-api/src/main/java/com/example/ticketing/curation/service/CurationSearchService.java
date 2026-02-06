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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurationSearchService {

    private final CurationRepository curationRepository;

    public CurationSearchResponse search(String keyword, CurationType type, String category, int page, int size) {
        String typeStr = type != null ? type.name() : null;
        int offset = page * size;

        List<Curation> curations = curationRepository.searchCurations(keyword, typeStr, category, size, offset);
        long totalElements = curationRepository.countSearchCurations(keyword, typeStr, category);

        List<CurationItem> items = curations.stream()
                .map(CurationItem::from)
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / size);
        Pagination pagination = new Pagination(page, size, totalElements, totalPages);

        return new CurationSearchResponse(items, pagination);
    }
}
