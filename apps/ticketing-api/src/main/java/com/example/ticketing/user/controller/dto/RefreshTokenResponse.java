package com.example.ticketing.user.controller.dto;

import com.example.ticketing.user.application.usecase.RefreshTokenUseCase;

/**
 * 토큰 갱신 응답 DTO
 */
public record RefreshTokenResponse(
    String accessToken,
    String refreshToken
) {

  /**
   * TokenPair를 RefreshTokenResponse로 변환
   */
  public static RefreshTokenResponse from(RefreshTokenUseCase.TokenPair tokenPair) {
    return new RefreshTokenResponse(
        tokenPair.accessToken(),
        tokenPair.refreshToken()
    );
  }
}
