package com.example.ticketing.user.controller;

import com.example.ticketing.user.application.dto.LoginResult;
import com.example.ticketing.user.application.usecase.KakaoLoginUseCase;
import com.example.ticketing.user.application.usecase.RefreshTokenUseCase;
import com.example.ticketing.user.controller.dto.LoginResponse;
import com.example.ticketing.user.controller.dto.MockLoginRequest;
import com.example.ticketing.user.controller.dto.RefreshTokenResponse;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import com.example.ticketing.user.infrastructure.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final KakaoLoginUseCase kakaoLoginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  /**
   * 카카오 로그인 콜백 (인가 코드 처리)
   * @return 로그인 응답 (JWT 토큰 + 사용자 정보)
   */
  @PostMapping("/kakao/callback")
  public ResponseEntity<LoginResponse> kakaoLogin(@RequestParam @NotBlank(message = "인가 코드는 필수입니다") String code) {
    LoginResult result = kakaoLoginUseCase.execute(code);
    return ResponseEntity.ok(LoginResponse.from(result));
  }

  /**
   * 카카오 로그인 콜백 (Query Parameter 방식)
   * 프론트엔드에서 Redirect로 받을 때 사용
   */
  @GetMapping("/kakao/callback")
  public ResponseEntity<LoginResponse> kakaoLoginCallback(@RequestParam String code) {
    LoginResult result = kakaoLoginUseCase.execute(code);
    return ResponseEntity.ok(LoginResponse.from(result));
  }

  /**
   * 토큰 갱신
   * @param authorization Authorization 헤더 (Bearer {refreshToken})
   * @return 새로운 Access Token, Refresh Token
   */
  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestHeader("Authorization") String authorization
  ) {
    String refreshToken = authorization.replace("Bearer ", "");
    RefreshTokenUseCase.TokenPair tokenPair = refreshTokenUseCase.execute(refreshToken);
    return ResponseEntity.ok(RefreshTokenResponse.from(tokenPair));
  }

  /**
   * Mock 로그인 (개발용)
   * 카카오 인증 없이 바로 JWT 토큰 발급
   * @param request 이메일, 닉네임
   * @return 로그인 응답 (JWT 토큰 + 사용자 정보)
   */
  @PostMapping("/mock/login")
  public ResponseEntity<LoginResponse> mockLogin(@Valid @RequestBody MockLoginRequest request) {
    // 기존 사용자 조회 또는 생성
    User user = userRepository.findByEmail(request.email())
        .orElseGet(() -> {
          User newUser = User.builder()
              .email(request.email())
              .nickname(request.nickname())
              .profileImage("https://via.placeholder.com/150")
              .build();
          return userRepository.save(newUser);
        });

    // JWT 토큰 발급
    String accessToken = jwtTokenProvider.createAccessToken(user.getId());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

    // 응답 생성
    LoginResult result = new LoginResult(
        accessToken,
        refreshToken,
        new LoginResult.UserInfo(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImage()
        )
    );

    return ResponseEntity.ok(LoginResponse.from(result));
  }
}
