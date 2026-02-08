package com.example.ticketing.mainpage.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.curation.repository.PopupRepository;
import com.example.ticketing.mainpage.dto.MainPageResponse;
import com.example.ticketing.mainpage.dto.MainPageResponse.CurationSummary;
import com.example.ticketing.user.domain.KoreanRegion;
import com.example.ticketing.user.domain.UserCategoryPreference;
import com.example.ticketing.user.domain.UserCategoryPreferenceRepository;
import com.example.ticketing.user.domain.UserPreferredRegion;
import com.example.ticketing.user.domain.UserPreferredRegionRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainPageService {

    private static final int MAX_ITEMS = 10;

    private final CurationRepository curationRepository;
    private final PopupRepository popupRepository;
    private final UserPreferredRegionRepository userPreferredRegionRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    public MainPageResponse getMainPageData(Long userId) {
        // 각 섹션을 안전하게 조회 (에러 시 빈 리스트 반환)
        List<CurationSummary> userCurations = safeGet(() -> getUserCurations(userId));
        List<CurationSummary> upcomingCurations = safeGet(this::getUpcomingCurations);
        List<CurationSummary> freeCurations = safeGet(this::getFreeCurations);
        List<CurationSummary> todayOpenCurations = safeGet(this::getTodayOpenCurations);

        return new MainPageResponse(userCurations, upcomingCurations, freeCurations, todayOpenCurations);
    }

    private List<CurationSummary> safeGet(java.util.function.Supplier<List<CurationSummary>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("메인페이지 데이터 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<CurationSummary> getUserCurations(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 유저 선호 지역 조회
        List<String> regions = userPreferredRegionRepository.findByUserId(userId).stream()
                .map(UserPreferredRegion::getRegion)
                .map(KoreanRegion::getDisplayName)
                .toList();

        // 유저 선호 카테고리 조회
        List<String> categories = userCategoryPreferenceRepository.findByUserId(userId).stream()
                .map(UserCategoryPreference::getCategory)
                .toList();

        if (regions.isEmpty() || categories.isEmpty()) {
            return Collections.emptyList();
        }

        // JSON 배열 형태로 변환: ["카테고리1", "카테고리2"]
        String categoriesJson = categories.stream()
                .map(c -> "\"" + c + "\"")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        // Native Query로 ID만 조회 후, findAllById로 엔티티 조회 (상속 구조 지원)
        List<Long> ids = curationRepository.findIdsByRegionsAndCategories(regions, categoriesJson);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return curationRepository.findAllById(ids).stream()
                .limit(MAX_ITEMS)
                .map(this::toCurationSummary)
                .toList();
    }

    private List<CurationSummary> getUpcomingCurations() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        return curationRepository.findUpcomingWithin7Days(today, sevenDaysLater).stream()
                .limit(MAX_ITEMS)
                .map(this::toCurationSummary)
                .toList();
    }

    private List<CurationSummary> getFreeCurations() {
        return popupRepository.findFreePopups().stream()
                .limit(MAX_ITEMS)
                .map(this::toCurationSummary)
                .toList();
    }

    private List<CurationSummary> getTodayOpenCurations() {
        LocalDate today = LocalDate.now();

        return curationRepository.findByStartDate(today).stream()
                .limit(MAX_ITEMS)
                .map(this::toCurationSummary)
                .toList();
    }

    private CurationSummary toCurationSummary(Curation curation) {
        String type = curation.getType() != null ? curation.getType().name() : null;
        return CurationSummary.of(
                curation.getId(),
                type,
                curation.getTitle(),
                curation.getSubTitle(),
                curation.getThumbnail(),
                curation.getRegion(),
                curation.getPlace(),
                curation.getStartDate(),
                curation.getEndDate(),
                curation.getCategory()
        );
    }
}
