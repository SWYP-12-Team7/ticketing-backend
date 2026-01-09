package com.example.ticketing.user.controller.dto;

import com.example.ticketing.user.domain.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    String nickname,
    String profileImage,
    LocalDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImage(),
            user.getCreatedAt()
        );
    }
}
