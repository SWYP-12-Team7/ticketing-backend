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
class UpdateUserAddressUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserAddressUseCase updateUserAddressUseCase;

    @Nested
    @DisplayName("execute 메서드")
    class Execute {

        // 주소 변경 성공: address, latitude, longitude가 업데이트된다
        @Test
        @DisplayName("성공: 주소 변경 시 주소와 좌표가 업데이트된다")
        void success() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@kakao.com")
                    .nickname("테스트유저")
                    .build();
            user.saveProfile("홍길동", "서울 성동구 성수동", 37.5445, 127.0567);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            updateUserAddressUseCase.execute(userId, "서울 강남구 역삼동", 37.5000, 127.0360);

            // then
            assertThat(user.getAddress()).isEqualTo("서울 강남구 역삼동");
            assertThat(user.getLatitude()).isEqualTo(37.5000);
            assertThat(user.getLongitude()).isEqualTo(127.0360);
        }

        // 주소만 변경되고 이름은 그대로인지 검증
        @Test
        @DisplayName("성공: 주소 변경 시 이름은 변경되지 않는다")
        void nameRemainsUnchanged() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@kakao.com")
                    .nickname("테스트유저")
                    .build();
            user.saveProfile("홍길동", "서울 성동구 성수동", 37.5445, 127.0567);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            updateUserAddressUseCase.execute(userId, "서울 강남구 역삼동", 37.5000, 127.0360);

            // then
            assertThat(user.getName()).isEqualTo("홍길동");
        }

        // 존재하지 않는 사용자: USER_NOT_FOUND 에러 발생
        @Test
        @DisplayName("실패: 존재하지 않는 사용자면 CustomException 발생")
        void failWhenUserNotFound() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> updateUserAddressUseCase.execute(
                    userId, "서울 강남구 역삼동", 37.5000, 127.0360))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }
}
