package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_category_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCategoryPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String category;

    @Builder
    public UserCategoryPreference(Long userId, String category) {
        this.userId = userId;
        this.category = category;
    }
}
