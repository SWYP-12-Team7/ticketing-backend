package com.example.ticketing.user.application.usecase;

import com.example.ticketing.user.domain.UserCategoryPreferenceRepository;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import com.example.ticketing.user.domain.UserRecentViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 조회 UseCase
 * TODO: 실제 구현 필요
 * - 찜한 행사 5개 요약
 * - 최근 열람 행사 5개 요약
 * - 온보딩 취향 카테고리 기반 행사 2개 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyPageUseCase {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRecentViewRepository userRecentViewRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    public Object execute(Long userId) {
        // TODO: 구현 예정
        // 1. 찜한 행사 5개 조회 (최신순)
        // 2. 최근 열람 행사 5개 조회 (최신순)
        // 3. 사용자 선호 카테고리 조회
        // 4. 선호 카테고리 기반 추천 행사 2개 조회
        // 5. DTO로 변환하여 반환
        log.info("TODO: GetMyPageUseCase - userId: {}", userId);
        return null;
    }
}