package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜하기 추가 UseCase
 * TODO: 실제 구현 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddFavoriteUseCase {

    private final UserFavoriteRepository userFavoriteRepository;

    public void execute(Long userId, Long curationId, CurationType curationType) {
        // TODO: 구현 예정
        // 1. 중복 체크
        // 2. UserFavorite 생성 및 저장
        // 3. Curation likeCount 증가
        log.info("TODO: AddFavoriteUseCase - userId: {}, curationId: {}, type: {}",
                userId, curationId, curationType);
    }
}