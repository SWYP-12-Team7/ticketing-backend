package com.example.ticketing.user.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.user.application.dto.SaveUserProfileRequest;
import com.example.ticketing.user.application.dto.UpdateUserAddressRequest;
import com.example.ticketing.user.application.dto.UserResponse;
import com.example.ticketing.user.application.usecase.SaveUserProfileUseCase;
import com.example.ticketing.user.application.usecase.UpdateUserAddressUseCase;
import com.example.ticketing.user.application.usecase.WithdrawUserUseCase;
import com.example.ticketing.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final WithdrawUserUseCase withdrawUserUseCase;
    private final SaveUserProfileUseCase saveUserProfileUseCase;
    private final UpdateUserAddressUseCase updateUserAddressUseCase;

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

    //나의 취향 (최근 열람 행사, 찜한 행사 요약, 온보딩 카테고리 기반 추천)


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
