package com.example.ticketing.curation.service;

import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Exhibition;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.dto.CurationSearchResponse;
import com.example.ticketing.curation.repository.ExhibitionRepository;
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
class CurationSearchServiceTest {

    @Autowired
    private CurationSearchService curationSearchService;

    @Autowired
    private PopupRepository popupRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    @BeforeEach
    void setUp() {
        popupRepository.deleteAll();
        exhibitionRepository.deleteAll();

        // 팝업 데이터 생성
        popupRepository.save(createPopup("봄날엔 팝업", "서울 용산구", List.of("패션", "뷰티")));
        popupRepository.save(createPopup("스누피 팝업스토어", "서울 성동구", List.of("캐릭터")));
        popupRepository.save(createPopup("카페 팝업", "부산 해운대구", List.of("F&B", "카페/디저트")));

        // 전시 데이터 생성
        exhibitionRepository.save(createExhibition("현대미술전", "서울 종로구", List.of("현대미술")));
        exhibitionRepository.save(createExhibition("사진전 봄", "서울 강남구", List.of("사진")));
    }

    private Popup createPopup(String title, String region, List<String> category) {
        return Popup.builder()
                .popupId(UUID.randomUUID().toString())
                .title(title)
                .region(region)
                .category(category)
                .tags(List.of("인기", "추천"))
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();
    }

    private Exhibition createExhibition(String title, String region, List<String> category) {
        Exhibition exhibition = Exhibition.builder()
                .title(title)
                .region(region)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();
        exhibition.applyEnrichment(category, List.of("전시", "아트"));
        return exhibition;
    }

    @Test
    @DisplayName("키워드로 검색하면 제목에 포함된 결과가 반환된다")
    void searchByKeyword() {
        // when
        CurationSearchResponse result = curationSearchService.search("봄", null, null, 0, 10);

        // then
        assertThat(result.curations()).hasSize(2);
        assertThat(result.curations())
                .extracting("title")
                .containsExactlyInAnyOrder("봄날엔 팝업", "사진전 봄");
    }

    @Test
    @DisplayName("행사 타입으로 필터링하면 해당 타입만 반환된다")
    void searchByType() {
        // when - POPUP만 조회
        CurationSearchResponse popupResult = curationSearchService.search(null, CurationType.POPUP, null, 0, 10);

        // then
        assertThat(popupResult.curations()).hasSize(3);
        assertThat(popupResult.curations())
                .allMatch(item -> item.type() == CurationType.POPUP);

        // when - EXHIBITION만 조회
        CurationSearchResponse exhibitionResult = curationSearchService.search(null, CurationType.EXHIBITION, null, 0, 10);

        // then
        assertThat(exhibitionResult.curations()).hasSize(2);
        assertThat(exhibitionResult.curations())
                .allMatch(item -> item.type() == CurationType.EXHIBITION);
    }

    @Test
    @DisplayName("카테고리로 필터링하면 해당 카테고리를 포함한 결과만 반환된다")
    void searchByCategory() {
        // when - 패션 카테고리
        CurationSearchResponse result = curationSearchService.search(null, null, "패션", 0, 10);

        // then
        assertThat(result.curations()).hasSize(1);
        assertThat(result.curations().get(0).title()).isEqualTo("봄날엔 팝업");
        assertThat(result.curations().get(0).location()).isEqualTo("서울 용산구");
    }

    @Test
    @DisplayName("타입과 카테고리를 함께 필터링할 수 있다")
    void searchByTypeAndCategory() {
        // when - POPUP + 캐릭터 카테고리
        CurationSearchResponse result = curationSearchService.search(null, CurationType.POPUP, "캐릭터", 0, 10);

        // then
        assertThat(result.curations()).hasSize(1);
        assertThat(result.curations().get(0).title()).isEqualTo("스누피 팝업스토어");
        assertThat(result.curations().get(0).type()).isEqualTo(CurationType.POPUP);
    }

    @Test
    @DisplayName("전시 카테고리로 필터링할 수 있다")
    void searchExhibitionByCategory() {
        // when - 현대미술 카테고리
        CurationSearchResponse result = curationSearchService.search(null, CurationType.EXHIBITION, "현대미술", 0, 10);

        // then
        assertThat(result.curations()).hasSize(1);
        assertThat(result.curations().get(0).title()).isEqualTo("현대미술전");
    }

    @Test
    @DisplayName("페이지네이션이 정상적으로 동작한다")
    void searchWithPagination() {
        // when - 첫 페이지, 2개씩
        CurationSearchResponse firstPage = curationSearchService.search(null, null, null, 0, 2);

        // then
        assertThat(firstPage.curations()).hasSize(2);
        assertThat(firstPage.pagination().page()).isEqualTo(0);
        assertThat(firstPage.pagination().size()).isEqualTo(2);
        assertThat(firstPage.pagination().totalElements()).isEqualTo(5);
        assertThat(firstPage.pagination().totalPages()).isEqualTo(3);

        // when - 두번째 페이지
        CurationSearchResponse secondPage = curationSearchService.search(null, null, null, 1, 2);

        // then
        assertThat(secondPage.curations()).hasSize(2);
        assertThat(secondPage.pagination().page()).isEqualTo(1);
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 리스트가 반환된다")
    void searchWithNoResults() {
        // when
        CurationSearchResponse result = curationSearchService.search("존재하지않는키워드", null, null, 0, 10);

        // then
        assertThat(result.curations()).isEmpty();
        assertThat(result.pagination().totalElements()).isEqualTo(0);
        assertThat(result.pagination().totalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("키워드와 타입을 함께 검색할 수 있다")
    void searchByKeywordAndType() {
        // when - "봄" 키워드 + POPUP 타입
        CurationSearchResponse result = curationSearchService.search("봄", CurationType.POPUP, null, 0, 10);

        // then
        assertThat(result.curations()).hasSize(1);
        assertThat(result.curations().get(0).title()).isEqualTo("봄날엔 팝업");
    }

    @Test
    @DisplayName("응답에 제목, 썸네일, 타입, 태그, 위치, 기간이 포함된다")
    void responseContainsRequiredFields() {
        // when
        CurationSearchResponse result = curationSearchService.search(null, CurationType.POPUP, null, 0, 10);

        // then
        var item = result.curations().get(0);
        assertThat(item.title()).isNotNull();
        assertThat(item.type()).isEqualTo(CurationType.POPUP);
        assertThat(item.tags()).isNotNull();
        assertThat(item.location()).isNotNull();
        assertThat(item.period()).isEqualTo("2026-01-01 ~ 2026-12-31");
    }
}
