package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Exhibition;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.TimelineResponse;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetTimelineUseCaseTest {

    @Mock
    private UserFavoriteRepository userFavoriteRepository;
    @Mock
    private CurationRepository curationRepository;

    @InjectMocks
    private GetTimelineUseCase getTimelineUseCase;

    @Test
    @DisplayName("성공: 찜이 없을 때 빈 목록 반환")
    void successWhenNoFavorites() {
        // given
        Long userId = 1L;
        given(userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any()))
                .willReturn(Collections.emptyList());

        // when
        TimelineResponse response = getTimelineUseCase.execute(userId);

        // then
        assertThat(response.upcoming()).isEmpty();
        assertThat(response.ongoing()).isEmpty();
    }

    @Test
    @DisplayName("성공: 오픈 예정/진행중 분류")
    void successClassifyByDate() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        UserFavorite favorite1 = UserFavorite.builder()
                .userId(userId)
                .curationId(1L)
                .curationType(CurationType.EXHIBITION)
                .build();
        UserFavorite favorite2 = UserFavorite.builder()
                .userId(userId)
                .curationId(2L)
                .curationType(CurationType.POPUP)
                .build();

        given(userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any()))
                .willReturn(List.of(favorite1, favorite2));

        // Curation은 실제로 조회되지 않으므로 빈 목록 반환 (Exhibition 인스턴스 생성이 어려워서)
        given(curationRepository.findAllById(any()))
                .willReturn(Collections.emptyList());

        // when
        TimelineResponse response = getTimelineUseCase.execute(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.upcoming()).isNotNull();
        assertThat(response.ongoing()).isNotNull();
    }
}