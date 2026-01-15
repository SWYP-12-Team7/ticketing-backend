package com.example.ticketing.curation.dto;

public record Pagination(
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}