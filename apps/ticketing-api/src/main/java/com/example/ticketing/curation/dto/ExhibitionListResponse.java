package com.example.ticketing.curation.dto;

import java.util.List;

public record ExhibitionListResponse(
    List<ExhibitionSummary> exhibitions,
    Pagination pagination
) {
    public static ExhibitionListResponse of(List<ExhibitionSummary> exhibitions, Pagination pagination) {
        return new ExhibitionListResponse(exhibitions, pagination);
    }
}
