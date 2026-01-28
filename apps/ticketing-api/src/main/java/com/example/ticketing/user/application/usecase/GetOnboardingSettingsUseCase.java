package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.application.dto.OnboardingSettingsResponse;
import com.example.ticketing.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOnboardingSettingsUseCase {

    private final UserRepository userRepository;
    private final UserPreferredRegionRepository userPreferredRegionRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    public OnboardingSettingsResponse execute(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<KoreanRegion> preferredRegions = userPreferredRegionRepository.findByUserId(userId).stream()
                .map(UserPreferredRegion::getRegion)
                .toList();

        List<String> categories = userCategoryPreferenceRepository.findByUserId(userId).stream()
                .map(UserCategoryPreference::getCategory)
                .toList();

        return new OnboardingSettingsResponse(preferredRegions, categories);
    }
}
