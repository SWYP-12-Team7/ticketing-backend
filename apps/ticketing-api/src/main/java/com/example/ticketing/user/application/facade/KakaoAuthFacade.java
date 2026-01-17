package com.example.ticketing.user.application.facade;

import com.example.ticketing.user.infrastructure.oauth.KakaoOAuthClient;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoTokenResponse;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 카카오 인증 Facade
 * 트랜잭션 외부에서 카카오 API 호출 처리
 */
@Component
@RequiredArgsConstructor
public class KakaoAuthFacade {

  private final KakaoOAuthClient kakaoOAuthClient;

  /**
   * 카카오 인가 코드로 사용자 정보 조회
   * @param code 카카오 인가 코드
   * @return 카카오 사용자 정보
   */
  public KakaoUserInfoResponse getKakaoUserInfo(String code) {
    // 1. 카카오 액세스 토큰 발급 (외부 I/O)
    KakaoTokenResponse tokenResponse = kakaoOAuthClient.getAccessToken(code);

    // 2. 카카오 사용자 정보 조회 (외부 I/O)
    return kakaoOAuthClient.getUserInfo(tokenResponse.accessToken());
  }
}
