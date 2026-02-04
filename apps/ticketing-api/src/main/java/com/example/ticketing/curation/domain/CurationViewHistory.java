package com.example.ticketing.curation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Curation 조회 이력 (Append-Only)
 * 기간별 랭킹 집계용으로 사용
 */
@Entity
@Table(name = "curation_view_history",
        indexes = {
                @Index(name = "idx_curation_id_viewed_at", columnList = "curation_id, viewed_at"),
                @Index(name = "idx_curation_type_viewed_at", columnList = "curation_type, viewed_at"),
                @Index(name = "idx_viewed_at", columnList = "viewed_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurationViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "curation_id", nullable = false)
    private Long curationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "curation_type", nullable = false, length = 20)
    private CurationType curationType;

    @Column(name = "user_id")
    private Long userId;  // 비로그인 사용자는 null

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Builder
    public CurationViewHistory(Long curationId, CurationType curationType, Long userId, LocalDateTime viewedAt) {
        this.curationId = curationId;
        this.curationType = curationType;
        this.userId = userId;
        this.viewedAt = viewedAt;
    }
}