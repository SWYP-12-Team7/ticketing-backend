package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddFavoriteUseCaseTest {

    @Mock
    private UserFavoriteRepository userFavoriteRepository;
    @Mock
    private CurationRepository curationRepository;

    @InjectMocks
    private AddFavoriteUseCase addFavoriteUseCase;

    @Test
    @DisplayName("실패: 이미 찜한 행사")
    void failWhenAlreadyFavorited() {
        // given
        Long userId = 1L;
        Long curationId = 100L;
        given(userFavoriteRepository.existsByUserIdAndCurationId(userId, curationId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> addFavoriteUseCase.execute(userId, curationId, CurationType.EXHIBITION))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_FAVORITED);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 행사")
    void failWhenCurationNotFound() {
        // given
        Long userId = 1L;
        Long curationId = 999L;
        given(userFavoriteRepository.existsByUserIdAndCurationId(userId, curationId))
                .willReturn(false);
        given(curationRepository.findById(curationId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addFavoriteUseCase.execute(userId, curationId, CurationType.EXHIBITION))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURATION_NOT_FOUND);
    }
}