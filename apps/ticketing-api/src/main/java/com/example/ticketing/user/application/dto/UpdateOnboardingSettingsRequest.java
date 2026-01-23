package com.example.ticketing.user.application.dto;

import java.util.List;

public record UpdateOnboardingSettingsRequest(
        List<String> categories,
        List<RegionDto> regions,
        Integer maxTravelTime
) {
}
