package com.example.ticketing.curation.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.dto.CalendarResponse;
import com.example.ticketing.curation.dto.CurationSearchResponse;
import com.example.ticketing.curation.dto.MapCurationResponse;
import com.example.ticketing.curation.dto.CurationKeywordResponse;
import com.example.ticketing.curation.dto.NearbyPlaceResponse;
import com.example.ticketing.curation.dto.ToggleFavoriteRequest;
import com.example.ticketing.curation.facade.CurationFacade;
import com.example.ticketing.curation.service.CalendarService;
import com.example.ticketing.curation.service.CurationSearchService;
import com.example.ticketing.curation.service.MapCurationService;
import com.example.ticketing.curation.service.CurationKeywordService;
import com.example.ticketing.curation.service.NearbyPlaceService;
import com.example.ticketing.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/curations")
@RequiredArgsConstructor
public class CurationController {

    private final CurationFacade curationFacade;
    private final MapCurationService mapCurationService;
    private final CalendarService calendarService;
    private final CurationSearchService curationSearchService;
    private final CurationKeywordService curationKeywordService;
    private final NearbyPlaceService nearbyPlaceService;

    @GetMapping("/search")
    @Operation(summary = "통합 검색", description = "검색어, 행사 타입(POPUP/EXHIBITION), 카테고리별 행사 목록 조회")
    public ResponseEntity<CurationSearchResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CurationType type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(curationSearchService.search(keyword, type, category, page, size));
    }

    @GetMapping("/map")
    @Operation(summary = "지도뷰 행사 조회", description = "해당 날짜에 진행 중인 행사 목록 (좌표 포함). 지역/카테고리 필터 적용 가능")
    public ResponseEntity<MapCurationResponse> getMapCurations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(mapCurationService.getMapCurations(date, region, category));
    }


    @GetMapping("/calendar")
    @Operation(summary = "캘린더뷰 행사 조회", description = "월별 일자별 팝업/전시 개수. 지역/카테고리 필터 적용 가능")
    public ResponseEntity<CalendarResponse> getCalendar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(calendarService.getCalendar(year, month, region, category));
    }


    @GetMapping("/calendar/list")
    @Operation(summary = "캘린더 날짜별 행사 리스트", description = "캘린더에서 날짜 클릭 시 해당 날짜 진행 중인 행사 목록")
    public ResponseEntity<MapCurationResponse> getCalendarList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(calendarService.getListByDate(date, region, category));
    }


    @GetMapping("/{curationId}/nearby")
    @Operation(summary = "주변 행사 조회", description = "해당 행사와 같은 지역의 진행 중인 행사 목록 (지도 표시용)")
    public ResponseEntity<MapCurationResponse> getNearbyCurations(
            @PathVariable Long curationId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(mapCurationService.getNearbyCurations(curationId, limit));
    }

    @GetMapping("/{curationId}/nearby-places")
    @Operation(summary = "주변 맛집/카페 조회", description = "행사 위치 기준 반경 1km 내 인기 식당 및 카페 목록")
    public ResponseEntity<NearbyPlaceResponse> getNearbyPlaces(
            @PathVariable Long curationId
    ) {
        return ResponseEntity.ok(nearbyPlaceService.getNearbyPlaces(curationId));
    }

    @GetMapping("/search/recommended")
    @Operation(summary = "추천 검색어 조회", description = "이번 주(일~토) 진행 중인 행사 태그 기반 인기 검색어 Top 10")
    public ResponseEntity<CurationKeywordResponse> getRecommendedKeywords() {
        return ResponseEntity.ok(curationKeywordService.getRecommendedKeywords());
    }

    @PostMapping("/favorites")
    public void toggleFavorite(@CurrentUser User user,
                               @RequestBody ToggleFavoriteRequest request) {
        curationFacade.toggle(
                user.getId(),
                request.curationId(),
                request.curationType()
        );
    }

}
