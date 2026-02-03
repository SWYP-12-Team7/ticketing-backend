package com.example.ticketing.user.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.user.application.dto.*;
import com.example.ticketing.user.application.usecase.*;
import com.example.ticketing.user.domain.KoreanRegion;
import com.example.ticketing.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding")
@Tag(name = "온보딩", description = "사용자 온보딩 API")
@RequiredArgsConstructor
public class OnboardingController {

    private final GetOnboardingCategoriesUseCase getCategoriesWithPopupUseCase;
    private final SaveOnboardingStep1UseCase saveOnboardingStep1UseCase;
    private final SaveOnboardingStep2UseCase saveOnboardingStep2UseCase;
    private final SkipOnboardingStep1UseCase skipOnboardingStep1UseCase;
    private final SkipOnboardingStep2UseCase skipOnboardingStep2UseCase;
    private final GetOnboardingSettingsUseCase getOnboardingSettingsUseCase;
    private final GetOnboardingStatusUseCase getOnboardingStatusUseCase;
    private final UpdateOnboardingRegionUseCase updateOnboardingSettingsUseCase;
    private final UpdateOnboardingCategoryUseCase updateOnboardingCategoryUseCase;

    @PostMapping("/step1")
    @Operation(summary = "Step 1: 관심 행사 지역 선택", description = "최소 1개, 최대 3개 선택")
    public ResponseEntity<Void> saveStep1(
            @CurrentUser User user,
            @RequestBody List<KoreanRegion> regions
    ) {
        saveOnboardingStep1UseCase.execute(user.getId(), regions);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/step2/categories")
    @Operation(summary = "Step 2: 카테고리 목록 조회", description = "각 카테고리별 대표 팝업 썸네일과 이름을 반환")
    public ResponseEntity<List<OnboardingCategoryResponse>> getStep2Categories() {
        List<OnboardingCategoryResponse> response = getCategoriesWithPopupUseCase.execute();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/step2")
    @Operation(summary = "Step 2: 관심 카테고리 선택", description = "카테고리별 대표 팝업을 보고 관심 카테고리 선택")
    public ResponseEntity<Void> saveStep2(
            @CurrentUser User user,
            @RequestBody List<String> categories
    ) {
        saveOnboardingStep2UseCase.execute(user.getId(), categories);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/step1/skip")
    @Operation(summary = "Step 1 건너뛰기", description = "관심 지역 선택을 건너뛰고 Step 2로 이동")
    public ResponseEntity<Void> skipStep1(@CurrentUser User user) {
        skipOnboardingStep1UseCase.execute(user.getId());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/step2/skip")
    @Operation(summary = "Step 2 건너뛰기", description = "관심 카테고리 선택을 건너뛰고 온보딩 완료")
    public ResponseEntity<Void> skipStep2(@CurrentUser User user) {
        skipOnboardingStep2UseCase.execute(user.getId());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/settings")
    @Operation(summary = "온보딩 설정 조회", description = "현재 사용자의 온보딩 설정 조회")
    public ResponseEntity<OnboardingSettingsResponse> getSettings(@CurrentUser User user) {
        OnboardingSettingsResponse response = getOnboardingSettingsUseCase.execute(user.getId());
        return ResponseEntity.ok(response);
    }


    @PutMapping("/settings/region")
    @Operation(summary = "온보딩 설정 수정", description = "관심 지역 수정")
    public ResponseEntity<Void> updateSettingsRegion(
            @CurrentUser User user,
            @RequestBody List<KoreanRegion> preferredRegions
    ) {
        updateOnboardingSettingsUseCase.execute(user.getId(), preferredRegions);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/settings/category")
    @Operation(summary = "온보딩 설정 수정", description = "관심 카테고리 수정")
    public ResponseEntity<Void> updateSettingsCategory(
            @CurrentUser User user,
            @RequestBody List<String> preferredCategories
    ) {
        updateOnboardingCategoryUseCase.execute(user.getId(), preferredCategories);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/status")
    @Operation(summary = "온보딩 진행 상태 조회", description = "건너뛰기 했던 단계는 재방문 시 '이어서 하시겠어요?' 판단용")
    public ResponseEntity<OnboardingStatusResponse> getStatus(@CurrentUser User user) {
        OnboardingStatusResponse response = getOnboardingStatusUseCase.execute(user.getId());
        return ResponseEntity.ok(response);
    }
}
