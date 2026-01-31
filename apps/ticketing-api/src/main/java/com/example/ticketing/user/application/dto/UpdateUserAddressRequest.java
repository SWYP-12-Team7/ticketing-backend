package com.example.ticketing.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserAddressRequest(
        @NotBlank String address,
        @NotNull Double latitude,
        @NotNull Double longitude
) {}
