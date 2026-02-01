package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferred_regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferredRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KoreanRegion region;

    @Builder
    public UserPreferredRegion(Long userId, KoreanRegion region) {
        this.userId = userId;
        this.region = region;
    }
}
