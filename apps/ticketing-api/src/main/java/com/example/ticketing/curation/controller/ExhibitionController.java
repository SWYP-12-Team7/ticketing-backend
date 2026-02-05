package com.example.ticketing.curation.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.curation.domain.CurationStatus;
import com.example.ticketing.curation.dto.ExhibitionDetailResponse;
import com.example.ticketing.curation.dto.ExhibitionListResponse;
import com.example.ticketing.curation.service.ExhibitionService;
import com.example.ticketing.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public ExhibitionListResponse getExhibitions(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) CurationStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser(required = false) User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = user != null ? user.getId() : null;
        return exhibitionService.getExhibitions(keyword, region, status, userId, pageable);
    }

    @GetMapping("/{exhibitionId}")
    public ExhibitionDetailResponse getExhibition(
        @PathVariable Long exhibitionId,
        @CurrentUser(required = false) User user
    ) {
        Long userId = user != null ? user.getId() : null;
        return exhibitionService.getExhibition(exhibitionId, userId);
    }

}
