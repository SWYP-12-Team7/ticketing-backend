package com.example.ticketing.user.application.dto;

/**
 * 로그인 결과 DTO
 * JWT 토큰과 사용자 정보를 담아 반환
 */
public record LoginResult(
    String accessToken,
    String refreshToken,
    UserInfo user
) {

  public record UserInfo(
      Long id,
      String email,
      String nickname,
      String profileImage
  ) {
  }
}
