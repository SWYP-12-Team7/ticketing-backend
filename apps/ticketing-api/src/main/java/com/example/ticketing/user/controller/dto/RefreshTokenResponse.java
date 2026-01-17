package com.example.ticketing.user.controller.dto;

import com.example.ticketing.user.application.usecase.RefreshTokenUseCase;


/**
 * 토큰 갱신 응답 DTO
 */
public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {


  public static RefreshTokenResponse from(RefreshTokenUseCase.TokenPair tokenPair) {
    return new RefreshTokenResponse(
            tokenPair.accessToken(),
            tokenPair.refreshToken()
    );
  }
}
