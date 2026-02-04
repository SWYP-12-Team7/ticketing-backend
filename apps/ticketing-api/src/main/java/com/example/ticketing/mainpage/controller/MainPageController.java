package com.example.ticketing.mainpage.controller;

import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.mainpage.dto.MainPageResponse;
import com.example.ticketing.mainpage.service.MainPageService;
import com.example.ticketing.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping
    public ApiResponse<MainPageResponse> getMainPage(@CurrentUser(required = false) User user) {
        Long userId = user != null ? user.getId() : null;
        log.info("메인페이지 조회 - userId: {}", userId);
        MainPageResponse response = mainPageService.getMainPageData(userId);
        return ApiResponse.success(response);
    }
}
