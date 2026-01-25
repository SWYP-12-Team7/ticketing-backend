package com.example.ticketing.user.infrastructure.oauth;

import com.example.ticketing.common.config.KakaoOAuthProperties;
import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoTokenResponse;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;


/**
 * 카카오 OAuth API 클라이언트
 * - 인가 코드로 액세스 토큰 발급
 * - 액세스 토큰으로 사용자 정보 조회
 */
@Slf4j
@Component
public class KakaoOAuthClient {

  private final RestClient restClient;
  private final KakaoOAuthProperties properties;

  public KakaoOAuthClient(KakaoOAuthProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder().build();
  }


  /**
   * 인가 코드로 액세스 토큰 발급
   * @param code 카카오 인가 코드
   * @return 카카오 토큰 응답 (액세스 토큰 포함)
   */
  public KakaoTokenResponse getAccessToken(String code) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", properties.clientId());
    params.add("client_secret", properties.clientSecret());
    params.add("redirect_uri", properties.redirectUri());
    params.add("code", code);

    log.info("카카오 토큰 요청 - URI: {}", properties.tokenUri());
    log.info("카카오 토큰 요청 - Params: grant_type={}, client_id={}, redirect_uri={}",
            params.getFirst("grant_type"),
            params.getFirst("client_id"),
            params.getFirst("redirect_uri"));

    try {
      KakaoTokenResponse response = restClient.post()
              .uri(properties.tokenUri())
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(params)
              .retrieve()
              .body(KakaoTokenResponse.class);

      log.info("카카오 액세스 토큰 발급 성공 - Token Type: {}, Access Token 존재: {}",
              response.tokenType(), response.accessToken() != null);

      if (response.accessToken() == null) {
        log.error("액세스 토큰이 null입니다. Response: {}", response);
        throw new CustomException(ErrorCode.KAKAO_AUTH_FAILED);
      }

      return response;
    } catch (Exception e) {
      log.error("카카오 액세스 토큰 발급 실패", e);
      throw new CustomException(ErrorCode.KAKAO_AUTH_FAILED);
    }
  }


  /**
   * 액세스 토큰으로 사용자 정보 조회
   * @param accessToken 카카오 액세스 토큰
   * @return 카카오 사용자 정보
   */
  public KakaoUserInfoResponse getUserInfo(String accessToken) {
    log.info("카카오 사용자 정보 요청 - URI: {}", properties.userInfoUri());
    log.info("카카오 사용자 정보 요청 - Access Token 앞 20자: {}",
            accessToken.substring(0, Math.min(20, accessToken.length())));

    try {
      KakaoUserInfoResponse response = restClient.get()
              .uri(properties.userInfoUri())
              .header("Authorization", "Bearer " + accessToken)
              .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
              .retrieve()
              .body(KakaoUserInfoResponse.class);

      log.info("카카오 사용자 정보 조회 성공 - User ID: {}", response.id());
      return response;
    } catch (Exception e) {
      log.error("카카오 사용자 정보 조회 실패 - Access Token: {}...",
              accessToken.substring(0, Math.min(20, accessToken.length())), e);
      throw new CustomException(ErrorCode.KAKAO_AUTH_FAILED);
    }
  }
}
