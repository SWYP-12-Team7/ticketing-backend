package com.example.ticketing.user.application.dto;

import java.util.List;

public record OnboardingStatusResponse(
        boolean completed,
        Integer currentStep,           // 현재 진행 단계 (1, 2, 3 또는 null)
        Integer lastContentIndex,      // Step3에서 마지막으로 응답한 인덱스
        boolean canResume,             // 이어서 할 수 있는지 여부
        List<String> savedCategories,  // 저장된 카테고리 (step1 완료 시)
        boolean hasRegions,            // 지역 저장 여부 (step2 완료 시)
        int savedPreferencesCount      // 저장된 취향 개수 (step3 진행 중)
) {
    public static OnboardingStatusResponse notStarted() {
        return new OnboardingStatusResponse(false, null, null, false, List.of(), false, 0);
    }

    public static OnboardingStatusResponse alreadyCompleted() {
        return new OnboardingStatusResponse(true, null, null, false, List.of(), false, 0);
    }
}