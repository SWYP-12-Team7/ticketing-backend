package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import com.example.ticketing.user.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 토큰 갱신 UseCase
 * Refresh Token으로 새로운 Access Token 발급
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenUseCase {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  /**
   * 토큰 갱신 실행
   * 
   * @param refreshToken Refresh Token
   * @return 새로운 Access Token, Refresh Token
   */
  public TokenPair execute(String refreshToken) {
    // 1. Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    // 2. Refresh Token에서 사용자 ID 추출
    Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

    // 3. 사용자 존재 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 4. 새로운 토큰 발급
    String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
    String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

    return new TokenPair(newAccessToken, newRefreshToken);
  }

  /**
   * 토큰 쌍 (Access Token, Refresh Token)
   */
  public record TokenPair(
      String accessToken,
      String refreshToken
  ) {
  }
}
