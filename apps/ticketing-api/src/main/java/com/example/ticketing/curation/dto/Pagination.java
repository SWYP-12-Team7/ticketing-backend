package com.example.ticketing.curation.dto;

import org.springframework.data.domain.Page;

public record Pagination(
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static Pagination from(Page<?> page) {
        return new Pagination(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
