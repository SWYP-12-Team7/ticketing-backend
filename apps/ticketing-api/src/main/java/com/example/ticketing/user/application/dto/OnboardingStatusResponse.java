package com.example.ticketing.user.application.dto;

import com.example.ticketing.user.domain.KoreanRegion;

import java.util.List;

public record OnboardingStatusResponse(
        boolean completed,
        Integer currentStep,                    // 현재 진행 단계 (1, 2 또는 null)
        boolean canResume,                      // 이어서 할 수 있는지 여부
        List<KoreanRegion> savedRegions,        // 저장된 관심 지역 (step1 완료 시)
        List<String> savedCategories            // 저장된 관심 카테고리 (step2 완료 시)
) {
    public static OnboardingStatusResponse notStarted() {
        return new OnboardingStatusResponse(false, null, false, List.of(), List.of());
    }

    public static OnboardingStatusResponse alreadyCompleted() {
        return new OnboardingStatusResponse(true, null, false, List.of(), List.of());
    }
}
