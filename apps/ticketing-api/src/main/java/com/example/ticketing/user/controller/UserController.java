package com.example.ticketing.user.controller;

import com.example.ticketing.user.application.usecase.WithdrawUserUseCase;
import com.example.ticketing.user.controller.dto.UserResponse;
import com.example.ticketing.user.application.usecase.FindUserUseCase;
import com.example.ticketing.user.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final FindUserUseCase findUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final WithdrawUserUseCase withdrawUserUseCase;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(findUserUseCase.findById(id));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        withdrawUserUseCase.execute(userId);

        return ResponseEntity.noContent().build();
    }
}
