package com.example.ticketing.curation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "exhibition_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exhibition_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExhibitionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exhibition_id", nullable = false)
    private Long exhibitionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExhibitionLike(Long userId, Long exhibitionId) {
        this.userId = userId;
        this.exhibitionId = exhibitionId;
        this.createdAt = LocalDateTime.now();
    }

    public static ExhibitionLike create(Long userId, Long exhibitionId) {
        return new ExhibitionLike(userId, exhibitionId);
    }
}
