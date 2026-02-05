package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.MyTasteResponse;
import com.example.ticketing.user.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetMyPageUseCaseTest {

    @Mock
    private UserFavoriteRepository userFavoriteRepository;
    @Mock
    private UserRecentViewRepository userRecentViewRepository;
    @Mock
    private UserCategoryPreferenceRepository userCategoryPreferenceRepository;
    @Mock
    private CurationRepository curationRepository;

    @InjectMocks
    private GetMyPageUseCase getMyPageUseCase;

    @Test
    @DisplayName("성공: 찜/최근열람/추천 목록 조회")
    void success() {
        // given
        Long userId = 1L;

        // 찜 목록 Mock
        UserFavorite favorite = UserFavorite.builder()
                .userId(userId)
                .curationId(100L)
                .curationType(CurationType.EXHIBITION)
                .build();
        given(userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
                .willReturn(List.of(favorite));

        // 최근 열람 Mock
        given(userRecentViewRepository.findByUserIdOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
                .willReturn(Collections.emptyList());

        // 카테고리 선호 Mock
        given(userCategoryPreferenceRepository.findByUserId(userId))
                .willReturn(Collections.emptyList());

        // Curation Mock
        given(curationRepository.findAllById(List.of(100L)))
                .willReturn(Collections.emptyList());
        given(curationRepository.findAllById(Collections.emptyList()))
                .willReturn(Collections.emptyList());

        // when
        MyTasteResponse response = getMyPageUseCase.execute(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.favorites()).isNotNull();
        assertThat(response.recentViews()).isNotNull();
        assertThat(response.recommendations()).isNotNull();
    }

    @Test
    @DisplayName("성공: 찜/최근열람이 없을 때 빈 목록 반환")
    void successWhenEmpty() {
        // given
        Long userId = 1L;

        given(userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
                .willReturn(Collections.emptyList());
        given(userRecentViewRepository.findByUserIdOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
                .willReturn(Collections.emptyList());
        given(userCategoryPreferenceRepository.findByUserId(userId))
                .willReturn(Collections.emptyList());
        given(curationRepository.findAllById(Collections.emptyList()))
                .willReturn(Collections.emptyList());

        // when
        MyTasteResponse response = getMyPageUseCase.execute(userId);

        // then
        assertThat(response.favorites()).isEmpty();
        assertThat(response.recentViews()).isEmpty();
        assertThat(response.recommendations()).isEmpty();
    }
}