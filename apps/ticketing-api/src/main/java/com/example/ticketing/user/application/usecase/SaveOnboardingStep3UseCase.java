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
public class SaveOnboardingStep3UseCase {

    private final UserRepository userRepository;
    private final UserContentPreferenceRepository preferenceRepository;

    public void execute(Long userId, List<PreferenceDto> preferences) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        preferenceRepository.deleteByUserId(userId);

        List<UserContentPreference> userPreferences = preferences.stream()
                .map(pref -> UserContentPreference.builder()
                        .userId(userId)
                        .contentId(pref.contentId())
                        .contentType(pref.contentType())
                        .preference(pref.preference())
                        .build())
                .toList();

        preferenceRepository.saveAll(userPreferences);
        user.completeOnboarding();
    }

    // UseCase 전용
    public record PreferenceDto(Long contentId,
                                ContentType contentType,
                                PreferenceType preference
    ) {}
}
