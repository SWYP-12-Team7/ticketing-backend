package com.example.ticketing.user.controller;

import com.example.ticketing.user.application.dto.LoginResult;
import com.example.ticketing.user.application.usecase.KakaoLoginUseCase;
import com.example.ticketing.user.application.usecase.RefreshTokenUseCase;
import com.example.ticketing.user.controller.dto.LoginResponse;
import com.example.ticketing.user.controller.dto.RefreshTokenResponse;
import com.example.ticketing.user.domain.UserRepository;
import com.example.ticketing.user.infrastructure.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "인증", description = "로그인, 회원가입, 토큰 관리 API")
@RequiredArgsConstructor
public class AuthController {

  private final KakaoLoginUseCase kakaoLoginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;


  @Operation(summary = "카카오 로그인 (POST)", description = "카카오 인가 코드로 로그인 처리 후 JWT 토큰 발급")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 인가 코드"),
      @ApiResponse(responseCode = "500", description = "카카오 API 호출 실패")
  })
  @PostMapping("/kakao/callback")
  public ResponseEntity<LoginResponse> kakaoLogin(
      @Parameter(description = "카카오 인가 코드", required = true)
      @RequestParam @NotBlank(message = "인가 코드는 필수입니다") String code) {
    LoginResult result = kakaoLoginUseCase.execute(code);
    return ResponseEntity.ok(LoginResponse.from(result));
  }


  @Operation(summary = "카카오 로그인 (GET)", description = "카카오 리다이렉트 콜백용. 프론트엔드에서 리다이렉트로 받을 때 사용")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 인가 코드")
  })
  @GetMapping("/kakao/callback")
  public ResponseEntity<LoginResponse> kakaoLoginCallback(
      @Parameter(description = "카카오 인가 코드", required = true)
      @RequestParam String code) {
    LoginResult result = kakaoLoginUseCase.execute(code);
    return ResponseEntity.ok(LoginResponse.from(result));
  }


  @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token과 Refresh Token 발급")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Bearer {refreshToken}", required = true)
      @RequestHeader("Authorization") String authorization) {
    String refreshToken = authorization.replace("Bearer ", "");
    RefreshTokenUseCase.TokenPair tokenPair = refreshTokenUseCase.execute(refreshToken);
    return ResponseEntity.ok(RefreshTokenResponse.from(tokenPair));
  }
}
