package com.example.ticketing.user.controller;

import com.example.ticketing.user.application.dto.OnboardingSettingsResponse;
import com.example.ticketing.user.application.dto.OnboardingStatusResponse;
import com.example.ticketing.user.application.dto.UpdateOnboardingSettingsRequest;
import com.example.ticketing.user.application.usecase.*;
import com.example.ticketing.user.domain.KoreanRegion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding")
@Tag(name = "온보딩", description = "사용자 온보딩 API")
@RequiredArgsConstructor
public class OnboardingController {

    private final SaveOnboardingStep1UseCase saveOnboardingStep1UseCase;
    private final SaveOnboardingStep2UseCase saveOnboardingStep2UseCase;
    private final GetOnboardingSettingsUseCase getOnboardingSettingsUseCase;
    private final GetOnboardingStatusUseCase getOnboardingStatusUseCase;
    private final UpdateOnboardingSettingsUseCase updateOnboardingSettingsUseCase;

    @PostMapping("/step1")
    @Operation(summary = "Step 1: 관심 행사 지역 선택", description = "최소 1개, 최대 3개 선택")
    public ResponseEntity<Void> saveStep1(
            @AuthenticationPrincipal Long userId,
            @RequestBody List<KoreanRegion> regions
    ) {
        saveOnboardingStep1UseCase.execute(userId, regions);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/step2")
    @Operation(summary = "Step 2: 관심 카테고리 선택", description = "카테고리별 대표 팝업을 보고 관심 카테고리 선택")
    public ResponseEntity<Void> saveStep2(
            @AuthenticationPrincipal Long userId,
            @RequestBody List<String> categories
    ) {
        saveOnboardingStep2UseCase.execute(userId, categories);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    @Operation(summary = "온보딩 설정 조회", description = "현재 사용자의 온보딩 설정 조회")
    public ResponseEntity<OnboardingSettingsResponse> getSettings(
            @AuthenticationPrincipal Long userId
    ) {
        OnboardingSettingsResponse response = getOnboardingSettingsUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/settings")
    @Operation(summary = "온보딩 설정 수정", description = "관심 지역과 관심 카테고리 수정")
    public ResponseEntity<Void> updateSettings(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateOnboardingSettingsRequest request
    ) {
        updateOnboardingSettingsUseCase.execute(
                userId,
                request.preferredRegions(),
                request.categories()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "온보딩 진행 상태 조회", description = "건너뛰기 했던 단계는 재방문 시 '이어서 하시겠어요?' 판단용")
    public ResponseEntity<OnboardingStatusResponse> getStatus(
            @AuthenticationPrincipal Long userId
    ) {
        OnboardingStatusResponse response = getOnboardingStatusUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }
}
