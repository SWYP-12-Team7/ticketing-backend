package com.example.ticketing.user.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.user.application.dto.*;
import com.example.ticketing.user.application.usecase.*;
import com.example.ticketing.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final WithdrawUserUseCase withdrawUserUseCase;
    private final SaveUserProfileUseCase saveUserProfileUseCase;
    private final UpdateUserAddressUseCase updateUserAddressUseCase;
    private final GetMyPageUseCase getMyPageUseCase;
    private final GetFavoriteListUseCase getFavoriteListUseCase;
    private final FolderUseCase folderUseCase;
    private final GetTimelineUseCase getTimelineUseCase;

    // 내 정보 설정 화면
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<UserResponse> getUser(@CurrentUser User user) {
        return ResponseEntity.ok(new UserResponse(
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getAddress()
        ));
    }

    @GetMapping("/me/taste")
    @Operation(summary = "나의 취향 조회", description = "찜한 행사 5개, 최근 열람 5개, 카테고리 기반 추천 2개")
    public ResponseEntity<MyTasteResponse> getMyTaste(@CurrentUser User user) {
        return ResponseEntity.ok(getMyPageUseCase.execute(user.getId()));
    }

    // ========== 찜 페이지 ==========
    @GetMapping("/me/favorites")
    @Operation(summary = "찜 목록 조회", description = "찜한 행사 목록 조회 (지역 필터링, 폴더 필터링 가능)")
    public ResponseEntity<FavoriteListResponse> getFavorites(
            @CurrentUser User user,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "crea 그리고  tedAt"));
        return ResponseEntity.ok(getFavoriteListUseCase.execute(user.getId(), region, folderId, pageable));
    }

    // ========== 타임라인 ==========

    @GetMapping("/me/timeline")
    @Operation(summary = "타임라인 조회", description = "찜한 행사 중 오픈 예정/진행중 목록 조회")
    public ResponseEntity<TimelineResponse> getTimeline(@CurrentUser User user) {
        return ResponseEntity.ok(getTimelineUseCase.execute(user.getId()));
    }

    // ========== 폴더 관리 ==========
    @GetMapping("/me/folders")
    @Operation(summary = "폴더 목록 조회")
    public ResponseEntity<List<FolderResponse>> getFolders(@CurrentUser User user) {
        return ResponseEntity.ok(folderUseCase.getFolders(user.getId()));
    }

    @PostMapping("/me/folders")
    @Operation(summary = "폴더 생성")
    public ResponseEntity<FolderResponse> createFolder(
            @CurrentUser User user,
            @RequestBody String name
    ) {
        return ResponseEntity.ok(folderUseCase.createFolder(user.getId(), name));
    }

    @PutMapping("/me/folders/{folderId}")
    @Operation(summary = "폴더 이름 수정")
    public ResponseEntity<Void> updateFolder(
            @CurrentUser User user,
            @PathVariable Long folderId,
            @RequestBody String name
    ) {
        folderUseCase.updateFolder(user.getId(), folderId, name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/folders/{folderId}")
    @Operation(summary = "폴더 삭제", description = "폴더 삭제 시 해당 폴더의 찜 항목은 미분류로 이동")
    public ResponseEntity<Void> deleteFolder(
            @CurrentUser User user,
            @PathVariable Long folderId
    ) {
        folderUseCase.deleteFolder(user.getId(), folderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/favorites/{favoriteId}/move")
    @Operation(summary = "찜 항목을 폴더로 이동", description = "folderId가 null이면 미분류로 이동")
    public ResponseEntity<Void> moveFavoriteToFolder(
            @CurrentUser User user,
            @PathVariable Long favoriteId,
            @RequestBody MoveFavoriteRequest request
    ) {
        folderUseCase.moveFavoriteToFolder(user.getId(), favoriteId, request.folderId());
        return ResponseEntity.ok().build();
    }


    // ========== 프로필 ==========

    @PostMapping("/profile")
    @Operation(summary = "이름/주소 최초 저장")
    public ResponseEntity<Void> saveProfile(
            @CurrentUser User user,
            @Valid @RequestBody SaveUserProfileRequest request) {
        saveUserProfileUseCase.execute(
                user.getId(), request.name(), request.address(),
                request.latitude(), request.longitude());
        return ResponseEntity.ok().build();
    }


    @PutMapping("/address")
    @Operation(summary = "주소 변경")
    public ResponseEntity<Void> updateAddress(
            @CurrentUser User user,
            @Valid @RequestBody UpdateUserAddressRequest request) {
        updateUserAddressUseCase.execute(
                user.getId(), request.address(),
                request.latitude(), request.longitude());
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/me")
    @Operation(summary = "회원탈퇴")
    public ResponseEntity<Void> withdrawUser(@CurrentUser User user) {
        withdrawUserUseCase.execute(user.getId());
        return ResponseEntity.noContent().build();
    }

}
