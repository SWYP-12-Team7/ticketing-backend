package com.example.ticketing.user.application.usecase;

import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜하기 취소 UseCase
 * TODO: 실제 구현 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RemoveFavoriteUseCase {

    private final UserFavoriteRepository userFavoriteRepository;

    public void execute(Long userId, Long curationId) {
        // TODO: 구현 예정
        // 1. 찜 데이터 조회
        // 2. 삭제
        // 3. Curation likeCount 감소
        log.info("TODO: RemoveFavoriteUseCase - userId: {}, curationId: {}", userId, curationId);
    }
}