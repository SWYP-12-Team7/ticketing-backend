package com.example.ticketing.user.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.user.application.usecase.WithdrawUserUseCase;
import com.example.ticketing.user.controller.dto.UserResponse;
import com.example.ticketing.user.application.usecase.FindUserUseCase;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.infrastructure.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final WithdrawUserUseCase withdrawUserUseCase;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<UserResponse> getUser(@CurrentUser User user) {
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage()
        ));
    }


    @DeleteMapping("/me")
    @Operation(summary = "회원탈퇴")
    public ResponseEntity<Void> withdrawUser(@CurrentUser User user) {
        withdrawUserUseCase.execute(user.getId());
        return ResponseEntity.noContent().build();
    }


    public record UserResponse(
            Long id,
            String email,
            String nickname,
            String profileImage
    ) {}

}
