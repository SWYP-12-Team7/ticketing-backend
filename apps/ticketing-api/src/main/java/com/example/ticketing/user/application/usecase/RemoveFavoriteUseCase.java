package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RemoveFavoriteUseCase {

    private final UserFavoriteRepository userFavoriteRepository;
    private final CurationRepository curationRepository;

    public void execute(Long userId, Long curationId) {
        // 찜 데이터 조회
        UserFavorite favorite = userFavoriteRepository.findByUserIdAndCurationId(userId, curationId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAVORITE_NOT_FOUND));

        // Curation likeCount 감소
        Curation curation = curationRepository.findById(curationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURATION_NOT_FOUND));
        curation.decrementLikeCount();

        // 삭제
        userFavoriteRepository.delete(favorite);
    }
}