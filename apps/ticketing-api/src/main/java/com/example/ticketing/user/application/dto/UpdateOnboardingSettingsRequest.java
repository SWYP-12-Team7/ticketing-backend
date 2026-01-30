package com.example.ticketing.user.application.dto;

import com.example.ticketing.user.domain.KoreanRegion;

import java.util.List;

public record UpdateOnboardingSettingsRequest(
        List<KoreanRegion> preferredRegions,
        List<String> categories
) {
}
