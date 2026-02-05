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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

  // 형용사 (2-3자)
  private static final List<String> ADJECTIVES = List.of(
          "행복한", "즐거운", "신나는", "설레는", "포근한",
          "따뜻한", "밝은", "맑은", "귀여운", "깜찍한",
          "용감한", "멋진", "씩씩한", "활발한", "상냥한",
          "다정한", "느긋한", "여유로", "호기심", "똑똑한"
  );

  // 명사 (2자)
  private static final List<String> NOUNS = List.of(
          "냥이", "멍이", "토끼", "여우", "판다",
          "곰이", "사슴", "다람", "펭귄", "부엉",
          "수달", "호랑", "사자", "코끼", "기린"
  );

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
            .nickname(generate())
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

  // 랜덤 닉네임 생성
  public String generate() {
    String adjective = ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size()));
    String noun = NOUNS.get(ThreadLocalRandom.current().nextInt(NOUNS.size()));
    int number = ThreadLocalRandom.current().nextInt(10, 100);  // 2자리 숫자

    // 형용사(3자) + 명사(2자) + 숫자(2자) = 7자
    return adjective + noun + number;
  }
}
