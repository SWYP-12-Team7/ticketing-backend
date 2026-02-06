package com.example.ticketing.curation.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.dto.MapCurationResponse;
import com.example.ticketing.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapCurationService {

    private final CurationRepository curationRepository;

    public MapCurationResponse getMapCurations(LocalDate date, String region, String category) {
        List<Curation> curations = curationRepository.findOngoingWithCoordinates(date);

        // 지역/카테고리 필터 적용 (Java에서 처리)
        List<MapCurationResponse.MapCurationItem> items = curations.stream()
                .filter(c -> region == null || region.equals(c.getRegion()))
                .filter(c -> category == null || (c.getCategory() != null && c.getCategory().contains(category)))
                .map(MapCurationResponse.MapCurationItem::from)
                .toList();

        return new MapCurationResponse(items);
    }
}