package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateOnboardingSettingsUseCase {

    private final UserRepository userRepository;
    private final UserPreferredRegionRepository userPreferredRegionRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    public void execute(Long userId, List<KoreanRegion> preferredRegions, List<String> categories) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 관심 지역 업데이트
        if (preferredRegions != null) {
            if (preferredRegions.isEmpty() || preferredRegions.size() > 3) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "관심 지역은 최소 1개, 최대 3개 선택해야 합니다.");
            }

            userPreferredRegionRepository.deleteByUserId(userId);

            List<UserPreferredRegion> regionList = preferredRegions.stream()
                    .map(region -> UserPreferredRegion.builder()
                            .userId(userId)
                            .region(region)
                            .build())
                    .toList();

            userPreferredRegionRepository.saveAll(regionList);
        }

        // 관심 카테고리 업데이트
        if (categories != null) {
            if (categories.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "관심 카테고리를 최소 1개 이상 선택해야 합니다.");
            }

            userCategoryPreferenceRepository.deleteByUserId(userId);

            List<UserCategoryPreference> categoryList = categories.stream()
                    .map(category -> UserCategoryPreference.builder()
                            .userId(userId)
                            .category(category)
                            .build())
                    .toList();

            userCategoryPreferenceRepository.saveAll(categoryList);
        }
    }
}
