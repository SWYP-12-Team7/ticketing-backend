package com.example.ticketing.user.infrastructure.oauth;

import com.example.ticketing.common.config.KakaoOAuthProperties;
import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoTokenResponse;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoUserInfoResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KakaoOAuthClientTest {

  private MockWebServer mockWebServer;
  private KakaoOAuthClient kakaoOAuthClient;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    String baseUrl = mockWebServer.url("/").toString();

    KakaoOAuthProperties properties = new KakaoOAuthProperties(
        "test-client-id",
        "test-client-secret",
        "http://localhost:8080/callback",
        baseUrl + "oauth/token",
        baseUrl + "v2/user/me"
    );

    kakaoOAuthClient = new KakaoOAuthClient(properties);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Nested
  @DisplayName("getAccessToken 메서드")
  class GetAccessToken {

    @Test
    @DisplayName("성공: 인가 코드로 액세스 토큰을 발급받는다")
    void success() throws InterruptedException {
      // given
      String code = "test-authorization-code";
      String tokenResponse = """
          {
            "token_type": "bearer",
            "access_token": "test-access-token",
            "expires_in": 21599,
            "refresh_token": "test-refresh-token",
            "refresh_token_expires_in": 5183999
          }
          """;

      mockWebServer.enqueue(new MockResponse()
          .setBody(tokenResponse)
          .addHeader("Content-Type", "application/json"));

      // when
      KakaoTokenResponse result = kakaoOAuthClient.getAccessToken(code);

      // then
      assertThat(result.accessToken()).isEqualTo("test-access-token");
      assertThat(result.tokenType()).isEqualTo("bearer");
      assertThat(result.refreshToken()).isEqualTo("test-refresh-token");

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getMethod()).isEqualTo("POST");
      assertThat(request.getBody().readUtf8()).contains("code=" + code);
    }

    @Test
    @DisplayName("실패: 카카오 서버 에러 시 CustomException 발생")
    void failWhenServerError() {
      // given
      String code = "invalid-code";

      mockWebServer.enqueue(new MockResponse()
          .setResponseCode(400)
          .setBody("{\"error\": \"invalid_grant\"}"));

      // when & then
      assertThatThrownBy(() -> kakaoOAuthClient.getAccessToken(code))
          .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("실패: 액세스 토큰이 null이면 CustomException 발생")
    void failWhenAccessTokenIsNull() {
      // given
      String code = "test-code";
      String tokenResponse = """
          {
            "token_type": "bearer",
            "access_token": null
          }
          """;

      mockWebServer.enqueue(new MockResponse()
          .setBody(tokenResponse)
          .addHeader("Content-Type", "application/json"));

      // when & then
      assertThatThrownBy(() -> kakaoOAuthClient.getAccessToken(code))
          .isInstanceOf(CustomException.class);
    }
  }

  @Nested
  @DisplayName("getUserInfo 메서드")
  class GetUserInfo {

    @Test
    @DisplayName("성공: 액세스 토큰으로 사용자 정보를 조회한다")
    void success() throws InterruptedException {
      // given
      String accessToken = "test-access-token";
      String userInfoResponse = """
          {
            "id": 12345678,
            "kakao_account": {
              "email": "test@kakao.com",
              "profile": {
                "nickname": "테스트유저",
                "profile_image_url": "http://example.com/image.jpg"
              }
            }
          }
          """;

      mockWebServer.enqueue(new MockResponse()
          .setBody(userInfoResponse)
          .addHeader("Content-Type", "application/json"));

      // when
      KakaoUserInfoResponse result = kakaoOAuthClient.getUserInfo(accessToken);

      // then
      assertThat(result.id()).isEqualTo(12345678L);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getMethod()).isEqualTo("GET");
      assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + accessToken);
    }

    @Test
    @DisplayName("실패: 유효하지 않은 토큰이면 CustomException 발생")
    void failWhenInvalidToken() {
      // given
      String invalidToken = "invalid-token";

      mockWebServer.enqueue(new MockResponse()
          .setResponseCode(401)
          .setBody("{\"error\": \"invalid_token\"}"));

      // when & then
      assertThatThrownBy(() -> kakaoOAuthClient.getUserInfo(invalidToken))
          .isInstanceOf(CustomException.class);
    }
  }
}