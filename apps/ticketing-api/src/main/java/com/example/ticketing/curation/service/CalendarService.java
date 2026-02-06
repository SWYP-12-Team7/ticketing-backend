package com.example.ticketing.curation.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.dto.CalendarResponse;
import com.example.ticketing.curation.dto.MapCurationResponse;
import com.example.ticketing.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CurationRepository curationRepository;

    public CalendarResponse getCalendar(int year, int month, String region, String category) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        // 해당 월과 겹치는 행사 조회
        List<Curation> curations = curationRepository.findOverlappingWithPeriod(firstDay, lastDay);

        // 지역/카테고리 필터
        List<Curation> filtered = curations.stream()
                .filter(c -> region == null || region.equals(c.getRegion()))
                .filter(c -> category == null || (c.getCategory() != null && c.getCategory().contains(category)))
                .toList();

        // 각 날짜별 카운트 계산
        List<CalendarResponse.DayCount> days = new ArrayList<>();
        for (int day = 1; day <= lastDay.getDayOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);

            long popupCount = countByTypeOnDate(filtered, CurationType.POPUP, date);
            long exhibitionCount = countByTypeOnDate(filtered, CurationType.EXHIBITION, date);

            days.add(new CalendarResponse.DayCount(date, popupCount, exhibitionCount));
        }

        return new CalendarResponse(year, month, days);
    }

    private long countByTypeOnDate(List<Curation> curations, CurationType type, LocalDate date) {
        return curations.stream()
                .filter(c -> c.getType() == type)
                .filter(c -> isOngoingOn(c, date))
                .count();
    }

    private boolean isOngoingOn(Curation c, LocalDate date) {
        if (c.getStartDate() == null) return false;
        if (c.getStartDate().isAfter(date)) return false;
        if (c.getEndDate() != null && c.getEndDate().isBefore(date)) return false;
        return true;
    }

    /**
     * 캘린더 날짜 클릭 시 해당 날짜 진행 중인 행사 리스트
     */
    public MapCurationResponse getListByDate(LocalDate date, String region, String category) {
        List<Curation> curations = curationRepository.findOngoingByDate(date);

        List<MapCurationResponse.MapCurationItem> items = curations.stream()
                .filter(c -> region == null || region.equals(c.getRegion()))
                .filter(c -> category == null || (c.getCategory() != null && c.getCategory().contains(category)))
                .map(MapCurationResponse.MapCurationItem::from)
                .toList();

        return new MapCurationResponse(items);
    }
}