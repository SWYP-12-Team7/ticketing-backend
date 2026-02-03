package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.CurationType;

public record ToggleFavoriteRequest(
        Long curationId,
        CurationType curationType
) {
}
