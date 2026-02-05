package com.example.ticketing.user.application.usecase;

import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.user.application.dto.LoginResult;
import com.example.ticketing.user.application.facade.KakaoAuthFacade;
import com.example.ticketing.user.domain.SocialAccount;
import com.example.ticketing.user.domain.SocialAccountRepository;
import com.example.ticketing.user.domain.SocialProvider;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import com.example.ticketing.user.infrastructure.oauth.dto.KakaoUserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class KakaoLoginUseCaseIntegrationTest {

  @Autowired
  private KakaoLoginUseCase kakaoLoginUseCase;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SocialAccountRepository socialAccountRepository;

  @MockitoBean
  private KakaoAuthFacade kakaoAuthFacade;

  @BeforeEach
  void setUp() {
    socialAccountRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Nested
  @DisplayName("execute 메서드 - 통합 테스트")
  class Execute {

    @Test
    @DisplayName("성공: 신규 유저 로그인 시 DB에 User와 SocialAccount가 저장된다")
    void createNewUserWhenFirstLogin() {
      // given
      String code = "test-authorization-code";
      Long kakaoUserId = 12345678L;
      String email = "newuser@kakao.com";
      String nickname = "신규유저";
      String profileImage = "http://example.com/profile.jpg";

      KakaoUserInfoResponse mockUserInfo = new KakaoUserInfoResponse(
          kakaoUserId,
          new KakaoUserInfoResponse.KakaoAccount(
              new KakaoUserInfoResponse.Profile(nickname, profileImage),
              email
          )
      );

      given(kakaoAuthFacade.getKakaoUserInfo(code)).willReturn(mockUserInfo);

      // when
      LoginResult result = kakaoLoginUseCase.execute(code);

      // then - 반환값 검증
      assertThat(result.accessToken()).isNotBlank();
      assertThat(result.refreshToken()).isNotBlank();
      assertThat(result.user().email()).isEqualTo(email);
      assertThat(result.user().nickname()).isNotBlank();  // 랜덤 닉네임 생성됨
      assertThat(result.user().nickname().length()).isLessThanOrEqualTo(7);  // 7자 이내

      // then - DB 저장 검증
      Optional<User> savedUser = userRepository.findByEmail(email);
      assertThat(savedUser).isPresent();
      assertThat(savedUser.get().getNickname()).isNotBlank();  // 랜덤 닉네임
      assertThat(savedUser.get().getProfileImage()).isEqualTo(profileImage);

      Optional<SocialAccount> savedSocialAccount = socialAccountRepository
          .findByProviderAndProviderId(SocialProvider.KAKAO, String.valueOf(kakaoUserId));
      assertThat(savedSocialAccount).isPresent();
      assertThat(savedSocialAccount.get().getUser().getId()).isEqualTo(savedUser.get().getId());
    }

    @Test
    @DisplayName("성공: 기존 유저 로그인 시 새로운 User를 생성하지 않는다")
    void returnExistingUserWhenAlreadyRegistered() {
      // given - 기존 유저 생성
      User existingUser = User.builder()
          .email("existing@kakao.com")
          .nickname("기존유저")
          .profileImage("http://example.com/old.jpg")
          .build();
      userRepository.save(existingUser);

      Long kakaoUserId = 99999999L;
      SocialAccount existingSocialAccount = SocialAccount.builder()
          .user(existingUser)
          .provider(SocialProvider.KAKAO)
          .providerId(String.valueOf(kakaoUserId))
          .build();
      socialAccountRepository.save(existingSocialAccount);

      String code = "test-authorization-code";
      KakaoUserInfoResponse mockUserInfo = new KakaoUserInfoResponse(
          kakaoUserId,
          new KakaoUserInfoResponse.KakaoAccount(
              new KakaoUserInfoResponse.Profile("기존유저", "http://example.com/old.jpg"),
              "existing@kakao.com"
          )
      );

      given(kakaoAuthFacade.getKakaoUserInfo(code)).willReturn(mockUserInfo);

      long userCountBefore = userRepository.count();

      // when
      LoginResult result = kakaoLoginUseCase.execute(code);

      // then - 기존 유저 반환
      assertThat(result.user().id()).isEqualTo(existingUser.getId());
      assertThat(result.user().email()).isEqualTo("existing@kakao.com");

      // then - 새로운 유저가 생성되지 않음
      long userCountAfter = userRepository.count();
      assertThat(userCountAfter).isEqualTo(userCountBefore);
    }
  }
}