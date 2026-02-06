package com.example.ticketing.mainpage.controller;

import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.mainpage.dto.MainPageResponse;
import com.example.ticketing.mainpage.dto.PopularCurationResponse;
import com.example.ticketing.mainpage.service.MainPageService;
import com.example.ticketing.mainpage.service.PopularCurationService;
import com.example.ticketing.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Main Page", description = "메인페이지 API")
@Slf4j
@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainPageController {

    private final MainPageService mainPageService;
    private final PopularCurationService popularCurationService;

    @GetMapping
    @Operation(summary = "메인페이지 조회", description = "유저 맞춤/오픈예정/무료/오늘오픈 행사 목록")
    public ApiResponse<MainPageResponse> getMainPage(@CurrentUser(required = false) User user) {
        Long userId = user != null ? user.getId() : null;
        log.info("메인페이지 조회 - userId: {}", userId);
        MainPageResponse response = mainPageService.getMainPageData(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기행사 조회", description = "24시간/이번주/이번달 기준 조회수 많은 순으로 팝업/전시 각각 조회")
    public ApiResponse<PopularCurationResponse> getPopularCurations(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("인기행사 조회 - limit: {}", limit);
        PopularCurationResponse response = popularCurationService.getPopularCurations(limit);
        return ApiResponse.success(response);
    }
}
