package com.example.ticketing.user.application.usecase;

import com.example.ticketing.user.application.dto.LoginResult;
import com.example.ticketing.user.application.facade.KakaoAuthFacade;
import com.example.ticketing.user.domain.SocialAccount;
import com.example.ticketing.user.domain.SocialAccountRepository;
import com.example.ticketing.user.domain.SocialProvider;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import com.example.ticketing.user.infrastructure.jwt.JwtTokenProvider;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카카오 로그인 UseCase
 * 1. Facade를 통해 카카오 사용자 정보 조회 (트랜잭션 외부)
 * 2. 기존 회원인지 확인 (SocialAccount 조회)
 * 3-1. 신규 회원 → User + SocialAccount 생성
 * 3-2. 기존 회원 → User 조회
 * 4. JWT 토큰 발급 및 반환
 */
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoLoginUseCase {

  private final KakaoAuthFacade kakaoAuthFacade;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final SocialAccountRepository socialAccountRepository;

  /**
   * 카카오 로그인 실행
   * @param code 카카오 인가 코드
   * @return 로그인 결과 (JWT 토큰 + 사용자 정보)
   */
  public LoginResult execute(String code) {
    // 1. Facade를 통해 카카오 사용자 정보 조회 (외부 I/O, 트랜잭션 외부)
    KakaoUserInfoResponse userInfo = kakaoAuthFacade.getKakaoUserInfo(code);

    // 2. 기존 회원인지 확인 및 처리 (트랜잭션 내부)
    User user = getOrCreateUser(userInfo);

    // 3. JWT 토큰 발급
    String accessToken = jwtTokenProvider.createAccessToken(user.getId());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

    // 4. 결과 반환
    return new LoginResult(
            accessToken,
            refreshToken,
            new LoginResult.UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImage()
            )
    );
  }


  private User getOrCreateUser(KakaoUserInfoResponse userInfo) {
    String providerId = String.valueOf(userInfo.id());

    // 소셜 계정으로 기존 회원 조회
    return socialAccountRepository.findByProviderAndProviderId(SocialProvider.KAKAO, providerId)
            .map(SocialAccount::getUser)
            .orElseGet(() -> createNewUser(userInfo));
  }


  private User createNewUser(KakaoUserInfoResponse userInfo) {
    // User 엔티티 생성 (전체 이메일 사용)
    User user = User.builder()
            .email(userInfo.extractEmail(true))  // true = 전체 이메일 사용
            .nickname(userInfo.getNickname())
            .profileImage(userInfo.getProfileImageUrl())
            .build();
    userRepository.save(user);

    // SocialAccount 엔티티 생성
    SocialAccount socialAccount = SocialAccount.builder()
            .user(user)
            .provider(SocialProvider.KAKAO)
            .providerId(String.valueOf(userInfo.id()))
            .build();
    socialAccountRepository.save(socialAccount);

    return user;
  }
}
