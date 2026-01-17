package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WithdrawUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private WithdrawUserUseCase withdrawUserUseCase;

  @Nested
  @DisplayName("execute 메서드")
  class Execute {

    @Test
    @DisplayName("성공: 회원 탈퇴 시 deletedAt이 설정된다")
    void success() {
      // given
      Long userId = 1L;
      User user = User.builder()
          .email("test@kakao.com")
          .nickname("테스트유저")
          .build();

      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      // when
      withdrawUserUseCase.execute(userId);

      // then
      assertThat(user.isDeleted()).isTrue();
      assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자면 CustomException 발생")
    void failWhenUserNotFound() {
      // given
      Long userId = 999L;
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> withdrawUserUseCase.execute(userId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
  }
}