package com.example.ticketing.collection.domain;

/**
 * 수집된 데이터의 검토 상태
 */
public enum ReviewStatus {
    PENDING_REVIEW,  // 검토 대기
    APPROVED,        // 승인됨
    REJECTED         // 거절됨
}
