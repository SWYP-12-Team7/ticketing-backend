package com.example.ticketing.curation.controller;

import com.example.ticketing.curation.domain.CurationStatus;
import com.example.ticketing.curation.dto.ExhibitionDetailResponse;
import com.example.ticketing.curation.dto.ExhibitionListResponse;
import com.example.ticketing.curation.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public ExhibitionListResponse getExhibitions(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) CurationStatus status,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return exhibitionService.getExhibitions(keyword, region, status, userId, pageable);
    }

    @GetMapping("/{exhibitionId}")
    public ExhibitionDetailResponse getExhibition(
        @PathVariable Long exhibitionId,
        @RequestParam(required = false) Long userId
    ) {
        return exhibitionService.getExhibition(exhibitionId, userId);
    }
}
