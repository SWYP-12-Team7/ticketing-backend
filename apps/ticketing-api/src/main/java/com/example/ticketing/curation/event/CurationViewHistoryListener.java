package com.example.ticketing.curation.event;

import com.example.ticketing.curation.domain.CurationViewHistory;
import com.example.ticketing.curation.repository.CurationViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Curation 조회 이벤트 리스너
 * 비동기로 조회 이력을 기록하여 메인 스레드 블로킹 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurationViewHistoryListener {

    private final CurationViewHistoryRepository curationViewHistoryRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCurationViewedEvent(CurationViewedEvent event) {
        try {
            CurationViewHistory history = CurationViewHistory.builder()
                    .curationId(event.getCurationId())
                    .curationType(event.getCurationType())
                    .userId(event.getUserId())
                    .viewedAt(event.getViewedAt())
                    .build();

            curationViewHistoryRepository.save(history);

            log.debug("조회 이력 저장 완료 - curationId: {}, type: {}, userId: {}, viewedAt: {}",
                    event.getCurationId(), event.getCurationType(), event.getUserId(), event.getViewedAt());

        } catch (Exception e) {
            log.error("조회 이력 저장 실패 - curationId: {}, type: {}",
                    event.getCurationId(), event.getCurationType(), e);
            // 이력 저장 실패는 메인 플로우에 영향을 주지 않음
        }
    }
}