package com.example.ticketing.curation.facade;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.user.application.usecase.AddFavoriteUseCase;
import com.example.ticketing.user.application.usecase.RemoveFavoriteUseCase;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CurationFacade {

    private final UserFavoriteRepository userFavoriteRepository;
    private final AddFavoriteUseCase addFavoriteUseCase;
    private final RemoveFavoriteUseCase removeFavoriteUseCase;

    @Transactional
    public void toggle(Long userId, Long curationId, CurationType type) {
        boolean exists = userFavoriteRepository.existsByUserIdAndCurationId(userId, curationId);

        if (exists) {
            // 이미 찜함 -> 찜 취소
            removeFavoriteUseCase.execute(userId, curationId);
        } else {
            // 아직 안함 -> 찜 추가
            addFavoriteUseCase.execute(userId, curationId, type);
        }
    }
}
