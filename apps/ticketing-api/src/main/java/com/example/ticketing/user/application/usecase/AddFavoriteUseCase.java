package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddFavoriteUseCase {

    private final UserFavoriteRepository userFavoriteRepository;
    private final CurationRepository curationRepository;

    public void execute(Long userId, Long curationId, CurationType curationType) {
        // 중복 체크
        if (userFavoriteRepository.existsByUserIdAndCurationId(userId, curationId)) {
            throw new CustomException(ErrorCode.ALREADY_FAVORITED);
        }

        // Curation 존재 확인 및 likeCount 증가
        Curation curation = curationRepository.findById(curationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURATION_NOT_FOUND));
        curation.incrementLikeCount();

        // UserFavorite 생성 및 저장
        UserFavorite favorite = UserFavorite.builder()
                .userId(userId)
                .curationId(curationId)
                .curationType(curationType)
                .build();
        userFavoriteRepository.save(favorite);
    }
}