package com.example.ticketing.user.application.dto;

import com.example.ticketing.user.domain.RegionTag;

import java.math.BigDecimal;
import java.util.List;


public record OnboardingSettingsResponse(
        List<String> categories,
        List<RegionInfo> regions,
        Integer maxTravelTime,
        List<PreferenceInfo> preferences
) {

    public record RegionInfo(
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            RegionTag tag
    ) {}

    public record PreferenceInfo(
            Long contentId,
            String contentType,
            String preference
    ) {}
}

