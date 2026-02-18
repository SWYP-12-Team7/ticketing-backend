package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.MyTasteResponse;
import com.example.ticketing.user.application.dto.MyTasteResponse.CurationSummary;
import com.example.ticketing.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyPageUseCase {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRecentViewRepository userRecentViewRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;
    private final CurationRepository curationRepository;

    private static final int SUMMARY_LIMIT = 5;
    private static final int RECOMMENDATION_LIMIT = 10;

    public MyTasteResponse execute(Long userId) {
        // 1. 찜한 행사 5개 조회 (최신순)
        List<CurationSummary> favorites = getFavorites(userId);

        // 2. 최근 열람 행사 5개 조회 (최신순)
        List<CurationSummary> recentViews = getRecentViews(userId);

        // 3. 선호 카테고리 기반 추천 행사 2개 조회
        List<CurationSummary> recommendations = getRecommendations(userId);

        return new MyTasteResponse(favorites, recentViews, recommendations);
    }

    private List<CurationSummary> getFavorites(Long userId) {
        List<UserFavorite> favorites = userFavoriteRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, SUMMARY_LIMIT));

        List<Long> curationIds = favorites.stream()
                .map(UserFavorite::getCurationId)
                .toList();

        return curationRepository.findAllById(curationIds).stream()
                .map(CurationSummary::from)
                .toList();
    }

    private List<CurationSummary> getRecentViews(Long userId) {
        List<UserRecentView> recentViews = userRecentViewRepository
                .findByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(0, SUMMARY_LIMIT));

        List<Long> curationIds = recentViews.stream()
                .map(UserRecentView::getCurationId)
                .toList();

        return curationRepository.findAllById(curationIds).stream()
                .map(CurationSummary::from)
                .toList();
    }

    private List<CurationSummary> getRecommendations(Long userId) {
        // 사용자 선호 카테고리 조회
        List<String> categories = userCategoryPreferenceRepository.findByUserId(userId).stream()
                .map(UserCategoryPreference::getCategory)
                .toList();

        if (categories.isEmpty()) {
            // 시연용: 카테고리 없으면 전시 목록 반환
            return curationRepository.findAll().stream()
                    .limit(RECOMMENDATION_LIMIT)
                    .map(CurationSummary::from)
                    .toList();
        }

        // JSON 배열 형태로 변환: ["카테고리1", "카테고리2"]
        String categoriesJson = categories.stream()
                .map(c -> "\"" + c + "\"")
                .collect(Collectors.joining(",", "[", "]"));

        // 카테고리 기반 랜덤 추천
        List<Curation> recommendations = curationRepository
                .findByCategoriesRandomly(categoriesJson, PageRequest.of(0, RECOMMENDATION_LIMIT));

        // 시연용: 추천 결과 없으면 전시 목록 반환
        if (recommendations.isEmpty()) {
            return curationRepository.findAll().stream()
                    .limit(RECOMMENDATION_LIMIT)
                    .map(CurationSummary::from)
                    .toList();
        }

        return recommendations.stream()
                .map(CurationSummary::from)
                .toList();
    }
}