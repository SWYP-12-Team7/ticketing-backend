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
public class SaveOnboardingStep1UseCase {

    private final UserRepository userRepository;
    private final UserPreferredRegionRepository userPreferredRegionRepository;

    public void execute(Long userId, List<KoreanRegion> regions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (regions == null || regions.isEmpty() || regions.size() > 3) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "관심 지역은 최소 1개, 최대 3개 선택해야 합니다.");
        }

        userPreferredRegionRepository.deleteByUserId(userId);

        List<UserPreferredRegion> userPreferredRegions = regions.stream()
                .map(region -> UserPreferredRegion.builder()
                        .userId(userId)
                        .region(region)
                        .build())
                .toList();

        userPreferredRegionRepository.saveAll(userPreferredRegions);

         //온보딩 진행 상태 업데이트 (step2로 이동)
        user.updateOnboardingStep(2);
    }
}
