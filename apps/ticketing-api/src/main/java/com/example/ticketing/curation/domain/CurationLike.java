package com.example.ticketing.curation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Curation 찜하기 (전시/팝업 통합)
 * - 사용자별 찜 목록 조회
 * - 찜 랭킹 계산 (created_at 기준)
 */
@Entity
@Table(
    name = "curation_likes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_curation_likes_user_curation",
        columnNames = {"user_id", "curation_id", "curation_type"}
    ),
    indexes = {
        @Index(name = "idx_curation_likes_user_id", columnList = "user_id"),
        @Index(name = "idx_curation_likes_curation", columnList = "curation_id, curation_type"),
        @Index(name = "idx_curation_likes_created_at", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurationLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "curation_id", nullable = false)
    private Long curationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "curation_type", nullable = false, length = 20)
    private CurationType curationType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private CurationLike(Long userId, Long curationId, CurationType curationType) {
        this.userId = userId;
        this.curationId = curationId;
        this.curationType = curationType;
        this.createdAt = LocalDateTime.now();
    }

    public static CurationLike create(Long userId, Long curationId, CurationType curationType) {
        return new CurationLike(userId, curationId, curationType);
    }
}