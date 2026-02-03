package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import com.example.ticketing.curation.domain.CurationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_recent_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRecentView extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "curation_id", nullable = false)
    private Long curationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "curation_type", nullable = false)
    private CurationType curationType;

    @Builder
    public UserRecentView(Long userId, Long curationId, CurationType curationType) {
        this.userId = userId;
        this.curationId = curationId;
        this.curationType = curationType;
    }

    public void updateViewedAt() {
        // BaseEntity의 updatedAt이 자동으로 갱신됨
    }
}