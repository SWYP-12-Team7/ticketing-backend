package com.example.ticketing.curation.controller;

import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.curation.dto.PopupDetailResponse;
import com.example.ticketing.curation.dto.PopupListResponse;
import com.example.ticketing.curation.facade.PopupFacade;
import com.example.ticketing.user.domain.User;
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
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser(required = false) User user
    ) {
        Long userId = user != null ? user.getId() : null;
        PopupListResponse response = popupFacade.getPopups(keyword, city, page, size, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{popupId}")
    public ApiResponse<PopupDetailResponse> getPopupDetail(
            @PathVariable String popupId,
            @CurrentUser(required = false) User user
    ) {
        Long userId = user != null ? user.getId() : null;
        PopupDetailResponse response = popupFacade.getPopupDetail(popupId, userId);
        return ApiResponse.success(response);
    }
}
