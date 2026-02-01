package com.example.ticketing.user.application.usecase;

import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Testcontainers MySQL을 사용한 통합 테스트 (Happy-Case)
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserProfileIntegrationTest {

    @Autowired
    private SaveUserProfileUseCase saveUserProfileUseCase;

    @Autowired
    private UpdateUserAddressUseCase updateUserAddressUseCase;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // 프로필 저장 후 DB에서 조회하여 실제 저장 여부 검증
    @Test
    @DisplayName("성공: 프로필 저장 후 DB에서 이름과 주소가 조회된다")
    void saveProfileAndVerifyInDb() {
        // given
        User user = User.builder()
                .email("profile@kakao.com")
                .nickname("프로필유저")
                .build();
        userRepository.save(user);

        // when
        saveUserProfileUseCase.execute(user.getId(), "홍길동", "서울 성동구 성수동", 37.5445, 127.0567);

        // then
        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
        assertThat(found.get().getAddress()).isEqualTo("서울 성동구 성수동");
        assertThat(found.get().getLatitude()).isEqualTo(37.5445);
        assertThat(found.get().getLongitude()).isEqualTo(127.0567);
    }

    // 주소 변경 후 DB에서 조회하여 업데이트 여부 검증
    @Test
    @DisplayName("성공: 주소 변경 후 DB에서 변경된 주소가 조회된다")
    void updateAddressAndVerifyInDb() {
        // given
        User user = User.builder()
                .email("address@kakao.com")
                .nickname("주소유저")
                .build();
        userRepository.save(user);
        saveUserProfileUseCase.execute(user.getId(), "김철수", "서울 성동구 성수동", 37.5445, 127.0567);

        // when
        updateUserAddressUseCase.execute(user.getId(), "서울 강남구 역삼동", 37.5000, 127.0360);

        // then
        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAddress()).isEqualTo("서울 강남구 역삼동");
        assertThat(found.get().getLatitude()).isEqualTo(37.5000);
        assertThat(found.get().getLongitude()).isEqualTo(127.0360);
        assertThat(found.get().getName()).isEqualTo("김철수"); // 이름은 변경되지 않음
    }
}
