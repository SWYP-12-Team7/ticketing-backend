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
    private final UserPreferredRegionRepository userPreferredRegionRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    public OnboardingStatusResponse execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isOnboardingCompleted()) {
            return OnboardingStatusResponse.alreadyCompleted();
        }

        if (user.getOnboardingStep() == null) {
            return OnboardingStatusResponse.notStarted();
        }

        List<KoreanRegion> savedRegions = userPreferredRegionRepository.findByUserId(userId).stream()
                .map(UserPreferredRegion::getRegion)
                .toList();

        List<String> savedCategories = userCategoryPreferenceRepository.findByUserId(userId).stream()
                .map(UserCategoryPreference::getCategory)
                .toList();

        return new OnboardingStatusResponse(
                false,
                user.getOnboardingStep(),
                true,
                savedRegions,
                savedCategories
        );
    }
}
