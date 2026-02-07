package com.example.ticketing.curation.service;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.dto.MapCurationResponse;
import com.example.ticketing.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
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

    /**
     * 주변 행사 조회 (같은 구 기준)
     */
    public MapCurationResponse getNearbyCurations(Long curationId, int limit) {
        Curation baseCuration = curationRepository.findById(curationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURATION_NOT_FOUND));

        // 주소에서 "OO구" 추출
        String district = extractDistrict(baseCuration.getAddress());
        if (district == null) {
            return new MapCurationResponse(Collections.emptyList());
        }

        List<Curation> nearbyCurations = curationRepository.findNearbyByDistrict(
                curationId,
                district,
                LocalDate.now(),
                limit
        );

        List<MapCurationResponse.MapCurationItem> items = nearbyCurations.stream()
                .map(MapCurationResponse.MapCurationItem::from)
                .toList();

        return new MapCurationResponse(items);
    }

    /**
     * 주소에서 "OO구" 추출
     * 예: "서울 서초구 남부순환로 2406" → "서초구"
     */
    private String extractDistrict(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 정규식으로 "OO구" 패턴 추출
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("([가-힣]+구)");
        java.util.regex.Matcher matcher = pattern.matcher(address);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}