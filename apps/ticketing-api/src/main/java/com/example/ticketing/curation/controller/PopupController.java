package com.example.ticketing.curation.controller;

import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.curation.dto.PopupDetailResponse;
import com.example.ticketing.curation.dto.PopupListResponse;
import com.example.ticketing.curation.facade.PopupFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/popups")
@RequiredArgsConstructor
public class PopupController {

    private final PopupFacade popupFacade;

    @GetMapping
    public ApiResponse<PopupListResponse> getPopups(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Long userId
    ) {
        PopupListResponse response = popupFacade.getPopups(keyword, region, page, size, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<PopupDetailResponse> getPopupDetail(
        @PathVariable Long id,
        @RequestParam(required = false) Long userId
    ) {
        PopupDetailResponse response = popupFacade.getPopupDetail(id, userId);
        return ApiResponse.success(response);
    }
}
