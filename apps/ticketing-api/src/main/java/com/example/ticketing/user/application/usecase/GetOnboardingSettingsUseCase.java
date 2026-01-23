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
    private final UserCategoryRepository userCategoryRepository;
    private final UserRegionRepository userRegionRepository;
    private final UserContentPreferenceRepository preferenceRepository;

    public OnboardingSettingsResponse execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<String> categories = userCategoryRepository.findByUserId(userId).stream()
                .map(UserCategory::getCategory)
                .toList();


        List<OnboardingSettingsResponse.RegionInfo> regions = userRegionRepository.findByUserId(userId).stream()
                        .map(region -> new OnboardingSettingsResponse.RegionInfo(
                                region.getAddress(),
                                region.getLatitude(),
                                region.getLongitude(),
                                region.getTag()
                        ))
                        .toList();


        List<OnboardingSettingsResponse.PreferenceInfo> preferences =
                preferenceRepository.findByUserId(userId).stream()
                        .map(pref -> new OnboardingSettingsResponse.PreferenceInfo(
                                pref.getContentId(),
                                pref.getContentType().name(),
                                pref.getPreference().name()
                        ))
                        .toList();

        return new OnboardingSettingsResponse(
                categories,
                regions,
                user.getMaxTravelTime(),
                preferences
        );
    }
}
