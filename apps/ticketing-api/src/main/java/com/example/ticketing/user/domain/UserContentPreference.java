package com.example.ticketing.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_content_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserContentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false, length = 20)
    private Long contentId;

    @Column(name = "content_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PreferenceType preference;

    @Builder
    public UserContentPreference(Long userId, Long contentId, ContentType contentType, PreferenceType preference) {
        this.userId = userId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.preference = preference;
    }

}
