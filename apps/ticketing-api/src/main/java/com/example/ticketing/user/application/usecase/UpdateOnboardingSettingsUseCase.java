package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.application.dto.RegionDto;
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
    private final UserCategoryRepository userCategoryRepository;
    private final UserRegionRepository userRegionRepository;

    public void execute(Long userId, List<String> categories, List<RegionDto> regions, Integer maxTravelTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 카테고리 검증
        if (categories != null && (categories.size() < 3 || categories.size() > 10)) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "카테고리는 최소 3개, 최대 10개 선택해야 합니다.");
        }

        // 카테고리 업데이트
        if (categories != null) {
            userCategoryRepository.deleteByUserId(userId);

            List<UserCategory> userCategories = categories.stream()
                    .map(category -> UserCategory.builder()
                            .userId(userId)
                            .category(category)
                            .build())
                    .toList();

            userCategoryRepository.saveAll(userCategories);
        }

        // 지역 업데이트
        // 지역 업데이트
        if (regions != null) {
            if (regions.isEmpty() || regions.size() > 3) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "지역은 최소 1개, 최대 3개 선택해야 합니다.");
            }
            userRegionRepository.deleteByUserId(userId);
            List<UserRegion> userRegions = regions.stream()
                    .map(region -> UserRegion.builder()
                            .userId(userId)
                            .address(region.address())
                            .latitude(region.latitude())
                            .longitude(region.longitude())
                            .tag(region.tag())
                            .build())
                    .toList();
            userRegionRepository.saveAll(userRegions);
        }

        // 최대 이동시간 업데이트
        if (maxTravelTime != null) {
            user.updateMaxTravelTime(maxTravelTime);
        }
    }
}
