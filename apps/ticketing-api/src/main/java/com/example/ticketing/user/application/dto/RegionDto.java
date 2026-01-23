package com.example.ticketing.user.application.dto;

import com.example.ticketing.user.domain.RegionTag;

import java.math.BigDecimal;

public record RegionDto(
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        RegionTag tag
) {
}
