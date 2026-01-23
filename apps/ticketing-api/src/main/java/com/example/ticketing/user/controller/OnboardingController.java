package com.example.ticketing.user.controller;

import com.example.ticketing.user.application.dto.OnboardingSettingsResponse;
import com.example.ticketing.user.application.dto.OnboardingStatusResponse;
import com.example.ticketing.user.application.dto.RegionDto;
import com.example.ticketing.user.application.dto.UpdateOnboardingSettingsRequest;
import com.example.ticketing.user.application.usecase.*;
import com.example.ticketing.user.controller.dto.ContentDto;
import com.example.ticketing.user.controller.dto.PreferenceRequest;
import com.example.ticketing.user.domain.ContentType;
import com.example.ticketing.user.domain.PreferenceType;
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
    private final SaveOnboardingStep3UseCase saveOnboardingStep3UseCase;
    private final SaveOnboardingStep3PartialUseCase saveOnboardingStep3PartialUseCase;
    private final GetOnboardingSettingsUseCase getOnboardingSettingsUseCase;
    private final GetOnboardingStatusUseCase getOnboardingStatusUseCase;
    private final UpdateOnboardingSettingsUseCase updateOnboardingSettingsUseCase;


    @PostMapping("/step1")
    @Operation(summary = "Step 1: 카테고리 선택", description = "최소 3개, 최대 10개 선택")
    public ResponseEntity<Void> saveStep1(
            @AuthenticationPrincipal Long userId,
            @RequestBody List<String> categories
    ) {
        saveOnboardingStep1UseCase.execute(userId, categories);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/step2")
    @Operation(summary = "Step 2: 지역 선택", description = "자주 가는 지역 1개 선택")
    public ResponseEntity<Void> saveStep2(
            @AuthenticationPrincipal Long userId,
            @RequestBody Step2Request request
    ) {
        saveOnboardingStep2UseCase.execute(userId, request.regions(), request.maxTravelTime());

        return ResponseEntity.ok().build();
    }


    @PostMapping("/step3")
    @Operation(summary = "Step 3: 취향 저장", description = "좋아요/싫어요 저장 및 온보딩 완료")
    public ResponseEntity<Void> saveStep3(
            @AuthenticationPrincipal Long userId,
            @RequestBody List<PreferenceRequest> preferences
    ) {
        List<SaveOnboardingStep3UseCase.PreferenceDto> dtos = preferences.stream()
                .map(req -> new SaveOnboardingStep3UseCase.PreferenceDto(
                        req.contentId(),
                        req.contentType(),
                        req.preference()
                ))
                .toList();

        saveOnboardingStep3UseCase.execute(userId, dtos);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/contents")
    @Operation(summary = "Step 3: 콘텐츠 목록 조회", description = "팝업 10개 반환")
    public ResponseEntity<List<ContentDto>> getContents() {
        List<ContentDto> mockData = List.of(
                new ContentDto(1L, ContentType.POPUP, "스누피 X 삼성 팝업스토어", "서울 강남", "http://image1.jpg"),
                new ContentDto(2L, ContentType.POPUP, "무민 팝업스토어", "서울 홍대", "http://image2.jpg"),
                new ContentDto(3L, ContentType.POPUP, "포켓몬 팝업", "부산 서면", "http://image3.jpg"),
                new ContentDto(4L, ContentType.POPUP, "디즈니 팝업", "서울 명동", "http://image4.jpg"),
                new ContentDto(5L, ContentType.POPUP, "캐릭터 팝업", "부산 해운대", "http://image5.jpg"),
                new ContentDto(6L, ContentType.POPUP, "K-POP 팝업", "서울 강남", "http://image6.jpg"),
                new ContentDto(7L, ContentType.POPUP, "아트 팝업", "서울 성수", "http://image7.jpg"),
                new ContentDto(8L, ContentType.POPUP, "패션 팝업", "서울 청담", "http://image8.jpg"),
                new ContentDto(9L, ContentType.POPUP, "뷰티 팝업", "서울 명동", "http://image9.jpg"),
                new ContentDto(10L, ContentType.POPUP, "F&B 팝업", "부산 광안리", "http://image10.jpg")
        );

        return ResponseEntity.ok(mockData);
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
        @Operation(summary = "온보딩 설정 수정", description = "카테고리와 지역 수정 (취향은 수정 불가)")
        public ResponseEntity<Void> updateSettings(
                @AuthenticationPrincipal Long userId,
                @RequestBody UpdateOnboardingSettingsRequest request
    ) {
            updateOnboardingSettingsUseCase.execute(
                    userId,
                    request.categories(),
                    request.regions(),
                    request.maxTravelTime()
            );

            return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "온보딩 진행 상태 조회", description = "중간 이탈 후 재방문 시 '이어서 하시겠어요?' 판단용")
    public ResponseEntity<OnboardingStatusResponse> getStatus(
            @AuthenticationPrincipal Long userId
    ) {
        OnboardingStatusResponse response = getOnboardingStatusUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/step3/partial")
    @Operation(summary = "Step 3: 취향 개별 저장", description = "매 응답마다 자동 저장 (중간 이탈 대응)")
    public ResponseEntity<Void> saveStep3Partial(
            @AuthenticationPrincipal Long userId,
            @RequestBody Step3PartialRequest request
    ) {
        SaveOnboardingStep3PartialUseCase.PreferenceDto dto =
                new SaveOnboardingStep3PartialUseCase.PreferenceDto(
                        request.contentId(),
                        request.contentType(),
                        request.preference()
                );

        saveOnboardingStep3PartialUseCase.execute(userId, dto, request.contentIndex());
        return ResponseEntity.ok().build();
    }


    public record Step2Request(
            List<RegionDto> regions,
            Integer maxTravelTime
    ) {}

    public record Step3PartialRequest(
            Long contentId,
            ContentType contentType,
            PreferenceType preference,
            int contentIndex  // 현재 응답 중인 콘텐츠 인덱스 (0-based)
    ) {}

}

