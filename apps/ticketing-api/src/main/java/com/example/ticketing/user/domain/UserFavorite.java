package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import com.example.ticketing.curation.domain.CurationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFavorite extends BaseEntity {

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

    @Column(name = "folder_id")
    private Long folderId;

    @Builder
    public UserFavorite(Long userId, Long curationId, CurationType curationType) {
        this.userId = userId;
        this.curationId = curationId;
        this.curationType = curationType;
    }

    public void moveToFolder(Long folderId) {
        this.folderId = folderId;
    }

    public void removeFromFolder() {
        this.folderId = null;
    }
}