package com.example.ticketing.user.infrastructure.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 OAuth 토큰 응답 DTO
 * 카카오 인가 코드로 액세스 토큰을 요청했을 때 받는 응답
 */
public record KakaoTokenResponse(
    @JsonProperty("token_type")
    String tokenType,
    
    @JsonProperty("access_token")
    String accessToken,
    
    @JsonProperty("expires_in")
    Integer expiresIn,
    
    @JsonProperty("refresh_token")
    String refreshToken,
    
    @JsonProperty("refresh_token_expires_in")
    Integer refreshTokenExpiresIn,
    
    @JsonProperty("scope")
    String scope
) {
}
