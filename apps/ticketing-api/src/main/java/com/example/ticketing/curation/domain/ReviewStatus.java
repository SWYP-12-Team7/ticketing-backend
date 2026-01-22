package com.example.ticketing.curation.domain;

public enum ReviewStatus {
    APPROVED,        // 승인됨 (신뢰도 0.8 이상)
    PENDING_REVIEW,  // 검토 대기 (신뢰도 0.5~0.8)
    REJECTED         // 거절됨
}
