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
     * 주변 행사 조회 (같은 구/시 기준)
     */
    public MapCurationResponse getNearbyCurations(Long curationId, int limit) {
        Curation baseCuration = curationRepository.findById(curationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURATION_NOT_FOUND));

        // 주소에서 "OO구" 또는 "OO시" 추출
        String district = extractDistrict(baseCuration.getAddress());
        if (district == null) {
            return new MapCurationResponse(Collections.emptyList());
        }

        // JPQL로 진행중인 행사 조회 후 Java에서 district 필터링
        List<Curation> allNearby = curationRepository.findNearbyOngoing(curationId, LocalDate.now());

        List<MapCurationResponse.MapCurationItem> items = allNearby.stream()
                .filter(c -> c.getAddress() != null && c.getAddress().contains(district))
                .limit(limit)
                .map(MapCurationResponse.MapCurationItem::from)
                .toList();

        return new MapCurationResponse(items);
    }

    /**
     * 주소에서 "OO구" 또는 "OO시" 추출
     * 예: "서울 서초구 남부순환로 2406" → "서초구"
     * 예: "강원 강릉시 경포로 123" → "강릉시"
     */
    private String extractDistrict(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 1. 먼저 "OO구" 패턴 찾기 (서울/광역시 내 구)
        java.util.regex.Pattern guPattern = java.util.regex.Pattern.compile("([가-힣]+구)");
        java.util.regex.Matcher guMatcher = guPattern.matcher(address);
        if (guMatcher.find()) {
            return guMatcher.group(1);
        }

        // 2. "OO구"가 없으면 "OO시" 패턴 찾기 (강릉시, 춘천시 등)
        java.util.regex.Pattern siPattern = java.util.regex.Pattern.compile("([가-힣]+시)");
        java.util.regex.Matcher siMatcher = siPattern.matcher(address);
        if (siMatcher.find()) {
            return siMatcher.group(1);
        }

        return null;
    }
}