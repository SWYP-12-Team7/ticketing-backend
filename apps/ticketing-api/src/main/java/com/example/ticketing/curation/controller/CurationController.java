package com.example.ticketing.curation.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.curation.dto.CalendarResponse;
import com.example.ticketing.curation.dto.MapCurationResponse;
import com.example.ticketing.curation.dto.ToggleFavoriteRequest;
import com.example.ticketing.curation.facade.CurationFacade;
import com.example.ticketing.curation.service.CalendarService;
import com.example.ticketing.curation.service.MapCurationService;
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
