package com.example.ticketing.user.infrastructure.jwt;

import com.example.ticketing.common.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    JwtProperties properties = new JwtProperties(
        "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
        3600000L,  // 1시간
        604800000L // 7일
    );
    jwtTokenProvider = new JwtTokenProvider(properties);
  }

  @Nested
  @DisplayName("createRefreshToken 메서드")
  class CreateRefreshToken {

    @Test
    @DisplayName("성공: 리프레시 토큰을 생성한다")
    void success() {
      // given
      Long userId = 1L;

      // when
      String refreshToken = jwtTokenProvider.createRefreshToken(userId);

      // then
      assertThat(refreshToken).isNotBlank();
      assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
      assertThat(jwtTokenProvider.getUserIdFromToken(refreshToken)).isEqualTo(userId);
    }
  }

  @Nested
  @DisplayName("validateToken 메서드")
  class ValidateToken {

    @Test
    @DisplayName("성공: 유효한 토큰이면 true 반환")
    void returnTrueWhenValid() {
      // given
      String token = jwtTokenProvider.createAccessToken(1L);

      // when
      boolean result = jwtTokenProvider.validateToken(token);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("실패: 만료된 토큰이면 false 반환")
    void returnFalseWhenExpired() {
      // given - 유효 시간이 0ms인 토큰 생성
      JwtProperties expiredProperties = new JwtProperties(
          "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
          0L,  // 즉시 만료
          0L
      );
      JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProperties);
      String expiredToken = expiredProvider.createAccessToken(1L);

      // when
      boolean result = expiredProvider.validateToken(expiredToken);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("실패: 잘못된 형식의 토큰이면 false 반환")
    void returnFalseWhenMalformed() {
      // given
      String malformedToken = "invalid.token.format";

      // when
      boolean result = jwtTokenProvider.validateToken(malformedToken);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("실패: 다른 키로 서명된 토큰이면 false 반환")
    void returnFalseWhenWrongSignature() {
      // given - 다른 키로 토큰 생성
      JwtProperties otherProperties = new JwtProperties(
          "another-secret-key-must-be-at-least-256-bits-long-for-hs256",
          3600000L,
          604800000L
      );
      JwtTokenProvider otherProvider = new JwtTokenProvider(otherProperties);
      String tokenWithOtherKey = otherProvider.createAccessToken(1L);

      // when
      boolean result = jwtTokenProvider.validateToken(tokenWithOtherKey);

      // then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("getUserIdFromToken 메서드")
  class GetUserIdFromToken {

    @Test
    @DisplayName("성공: 토큰에서 사용자 ID를 추출한다")
    void success() {
      // given
      Long userId = 123L;
      String token = jwtTokenProvider.createAccessToken(userId);

      // when
      Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

      // then
      assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("실패: 유효하지 않은 토큰이면 예외 발생")
    void throwExceptionWhenInvalid() {
      // given
      String invalidToken = "invalid.token";

      // when & then
      assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
          .isInstanceOf(Exception.class);
    }
  }
}
