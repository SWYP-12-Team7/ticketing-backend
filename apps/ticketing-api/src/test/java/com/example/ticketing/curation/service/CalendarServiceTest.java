package com.example.ticketing.curation.service;

import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.dto.CalendarResponse;
import com.example.ticketing.curation.dto.MapCurationResponse;
import com.example.ticketing.curation.repository.PopupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CalendarServiceTest {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private PopupRepository popupRepository;

    @BeforeEach
    void setUp() {
        popupRepository.deleteAll();

        // 2월 1일~15일 진행 팝업
        popupRepository.save(createPopup("2월 초 팝업", "서울", List.of("팝업"),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15)));

        // 2월 10일~28일 진행 팝업
        popupRepository.save(createPopup("2월 중순 팝업", "서울", List.of("팝업"),
                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 28)));

        // 2월 전체 진행 (부산)
        popupRepository.save(createPopup("부산 팝업", "부산", List.of("팝업"),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)));
    }

    private Popup createPopup(String title, String region, List<String> category,
                              LocalDate start, LocalDate end) {
        return Popup.builder()
                .popupId(UUID.randomUUID().toString())
                .title(title)
                .region(region)
                .category(category)
                .startDate(start)
                .endDate(end)
                .build();
    }

    @Test
    @DisplayName("캘린더 - 일별 팝업/전시 개수가 반환된다")
    void shouldReturnDailyCounts() {
        // when
        CalendarResponse result = calendarService.getCalendar(2026, 2, null, null);

        // then
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(2);
        assertThat(result.days()).hasSize(28); // 2월은 28일

        // 2월 1일: 2개 (2월 초 팝업, 부산 팝업)
        CalendarResponse.DayCount day1 = result.days().get(0);
        assertThat(day1.date()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(day1.popupCount()).isEqualTo(2);

        // 2월 10일: 3개 (2월 초, 2월 중순, 부산)
        CalendarResponse.DayCount day10 = result.days().get(9);
        assertThat(day10.popupCount()).isEqualTo(3);

        // 2월 20일: 2개 (2월 중순, 부산) - 2월 초는 15일에 종료
        CalendarResponse.DayCount day20 = result.days().get(19);
        assertThat(day20.popupCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("캘린더 - 지역 필터가 적용된다")
    void shouldFilterByRegion() {
        // when
        CalendarResponse result = calendarService.getCalendar(2026, 2, "서울", null);

        // then - 서울만 (부산 제외)
        CalendarResponse.DayCount day1 = result.days().get(0);
        assertThat(day1.popupCount()).isEqualTo(1); // 2월 초 팝업만
    }

    @Test
    @DisplayName("캘린더 리스트 - 해당 날짜 진행중인 행사가 반환된다")
    void shouldReturnListByDate() {
        // when
        MapCurationResponse result = calendarService.getListByDate(
                LocalDate.of(2026, 2, 10), null, null);

        // then - 2월 10일: 3개
        assertThat(result.items()).hasSize(3);
    }

    @Test
    @DisplayName("캘린더 리스트 - 지역 필터가 적용된다")
    void shouldFilterListByRegion() {
        // when
        MapCurationResponse result = calendarService.getListByDate(
                LocalDate.of(2026, 2, 10), "서울", null);

        // then - 서울만 2개
        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).allMatch(item -> item.title().contains("팝업"));
    }
}