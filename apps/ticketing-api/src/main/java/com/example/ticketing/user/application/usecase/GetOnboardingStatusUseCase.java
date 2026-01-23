package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.application.dto.OnboardingStatusResponse;
import com.example.ticketing.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOnboardingStatusUseCase {

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final UserRegionRepository userRegionRepository;
    private final UserContentPreferenceRepository preferenceRepository;

    public OnboardingStatusResponse execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 온보딩 완료된 경우
        if (user.isOnboardingCompleted()) {
            return OnboardingStatusResponse.alreadyCompleted();
        }

        // 온보딩 시작하지 않은 경우
        if (user.getOnboardingStep() == null) {
            return OnboardingStatusResponse.notStarted();
        }

        // 진행 중인 경우 - 저장된 데이터 조회
        List<String> savedCategories = userCategoryRepository.findByUserId(userId).stream()
                .map(UserCategory::getCategory)
                .toList();

        boolean hasRegions = !userRegionRepository.findByUserId(userId).isEmpty();

        int savedPreferencesCount = preferenceRepository.findByUserId(userId).size();

        return new OnboardingStatusResponse(
                false,
                user.getOnboardingStep(),
                user.getLastContentIndex(),
                true,  // canResume = true
                savedCategories,
                hasRegions,
                savedPreferencesCount
        );
    }
}