package com.example.ticketing.curation.domain;

import com.example.jpa.domain.BaseEntity;
import com.example.ticketing.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popup_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "popup_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopupLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id", nullable = false)
    private Popup popup;

    @Builder
    public PopupLike(User user, Popup popup) {
        this.user = user;
        this.popup = popup;
    }
}