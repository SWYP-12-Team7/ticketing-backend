package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.user.domain.UserRecentViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 최근 열람 행사 기록 UseCase
 * TODO: 실제 구현 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecordRecentViewUseCase {

    private final UserRecentViewRepository userRecentViewRepository;

    public void execute(Long userId, Long curationId, CurationType curationType) {
        // TODO: 구현 예정
        // 1. 기존 기록이 있는지 확인
        // 2. 있으면 updatedAt 갱신, 없으면 새로 생성
        // 3. Curation viewCount 증가
        log.info("TODO: RecordRecentViewUseCase - userId: {}, curationId: {}, type: {}",
                userId, curationId, curationType);
    }
}