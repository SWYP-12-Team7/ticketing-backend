package com.example.ticketing.user.controller.dto;

import com.example.ticketing.user.domain.ContentType;
import com.example.ticketing.user.domain.PreferenceType;

public record PreferenceRequest(
        Long contentId,
        ContentType contentType,
        PreferenceType preference
) {}
