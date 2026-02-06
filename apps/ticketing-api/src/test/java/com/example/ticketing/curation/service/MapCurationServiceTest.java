package com.example.ticketing.curation.service;

import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.curation.domain.Popup;
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
class MapCurationServiceTest {

    @Autowired
    private MapCurationService mapCurationService;

    @Autowired
    private PopupRepository popupRepository;

    @BeforeEach
    void setUp() {
        popupRepository.deleteAll();

        // 서울 팝업 (진행중, 좌표 있음)
        popupRepository.save(createPopup("서울 팝업", "서울", List.of("팝업"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), 37.5665, 126.9780));

        // 부산 팝업 (진행중, 좌표 있음)
        popupRepository.save(createPopup("부산 팝업", "부산", List.of("팝업"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), 35.1796, 129.0756));

        // 종료된 팝업
        popupRepository.save(createPopup("종료된 팝업", "서울", List.of("팝업"),
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31), 37.5665, 126.9780));

        // 좌표 없는 팝업
        popupRepository.save(createPopup("좌표없는 팝업", "서울", List.of("팝업"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), null, null));
    }

    private Popup createPopup(String title, String region, List<String> category,
                              LocalDate start, LocalDate end, Double lat, Double lng) {
        return Popup.builder()
                .popupId(UUID.randomUUID().toString())
                .title(title)
                .region(region)
                .category(category)
                .startDate(start)
                .endDate(end)
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    @Test
    @DisplayName("진행중인 행사만 조회된다")
    void shouldReturnOnlyOngoingCurations() {
        // when
        MapCurationResponse result = mapCurationService.getMapCurations(
                LocalDate.of(2026, 2, 6), null, null);

        // then - 좌표 있고 진행중인 2개만 조회 (종료된 것, 좌표 없는 것 제외)
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("지역 필터가 적용된다")
    void shouldFilterByRegion() {
        // when
        MapCurationResponse result = mapCurationService.getMapCurations(
                LocalDate.of(2026, 2, 6), "서울", null);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("서울 팝업");
    }

    @Test
    @DisplayName("카테고리 필터가 적용된다")
    void shouldFilterByCategory() {
        // given - 전시 카테고리 추가
        popupRepository.save(createPopup("전시회", "서울", List.of("전시"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), 37.5665, 126.9780));

        // when
        MapCurationResponse result = mapCurationService.getMapCurations(
                LocalDate.of(2026, 2, 6), null, "전시");

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("전시회");
    }

    @Test
    @DisplayName("응답에 좌표가 포함된다")
    void shouldIncludeCoordinates() {
        // when
        MapCurationResponse result = mapCurationService.getMapCurations(
                LocalDate.of(2026, 2, 6), "서울", null);

        // then
        assertThat(result.items().get(0).latitude()).isNotNull();
        assertThat(result.items().get(0).longitude()).isNotNull();
    }
}