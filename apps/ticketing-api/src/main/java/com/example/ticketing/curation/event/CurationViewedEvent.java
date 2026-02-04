package com.example.ticketing.curation.event;

import com.example.ticketing.curation.domain.CurationType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Curation 조회 이벤트
 * 비동기로 조회 이력을 기록하기 위한 이벤트
 */
@Getter
public class CurationViewedEvent {

    private final Long curationId;
    private final CurationType curationType;
    private final Long userId;  // 비로그인 사용자는 null
    private final LocalDateTime viewedAt;

    public CurationViewedEvent(Long curationId, CurationType curationType, Long userId) {
        this.curationId = curationId;
        this.curationType = curationType;
        this.userId = userId;
        this.viewedAt = LocalDateTime.now();
    }
}