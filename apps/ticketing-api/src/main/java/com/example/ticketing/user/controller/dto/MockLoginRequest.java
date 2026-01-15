package com.example.ticketing.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Mock 로그인 요청 DTO (개발용)
 * 카카오 인증 없이 바로 로그인할 수 있는 API용
 */
public record MockLoginRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "닉네임은 필수입니다")
    String nickname
) {
}
