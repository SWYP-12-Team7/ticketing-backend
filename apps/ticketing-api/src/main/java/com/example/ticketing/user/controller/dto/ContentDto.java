package com.example.ticketing.user.controller.dto;

import com.example.ticketing.user.domain.ContentType;


public record ContentDto(
        Long id,
        ContentType type,
        String title,
        String location,
        String imageUrl
) {}
