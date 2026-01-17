package com.example.ticketing.user.application.dto;

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
