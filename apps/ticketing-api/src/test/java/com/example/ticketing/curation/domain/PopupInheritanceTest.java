package com.example.ticketing.curation.domain;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.curation.repository.PopupRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PopupInheritanceTest {

    @Autowired
    private PopupRepository popupRepository;

    @Autowired
    private PopupRawRepository popupRawRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Popup 저장 시 curation과 popup 테이블 모두에 데이터가 저장된다")
    void savePopupToInheritedTables() {
        // given
        PopupRaw raw = PopupRaw.builder()
                .popupId("test-popup-id")
                .title("테스트 팝업스토어")
                .thumbnailImageUrl("https://example.com/thumb.jpg")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .city("서울")
                .district("성동구")
                .placeName("성수동 테스트 장소")
                .category(List.of("패션", "뷰티"))
                .tags(List.of("인기", "신규"))
                .isFree(true)
                .reservationRequired(false)
                .homepageUrl("https://example.com")
                .snsUrl("https://instagram.com/test")
                .reviewStatus(ReviewStatus.APPROVED)
                .build();

        // when
        Popup popup = Popup.fromRaw(raw);
        popupRepository.save(popup);

        // flush to ensure data is written to DB
        entityManager.flush();
        entityManager.clear();

        // then - curation 테이블 확인
        List<Map<String, Object>> curationRows = jdbcTemplate.queryForList(
                "SELECT * FROM curation WHERE type = 'POPUP'"
        );
        assertThat(curationRows).hasSize(1);

        Map<String, Object> curationRow = curationRows.get(0);
        assertThat(curationRow.get("title")).isEqualTo("테스트 팝업스토어");
        assertThat(curationRow.get("thumbnail")).isEqualTo("https://example.com/thumb.jpg");
        assertThat(curationRow.get("region")).isEqualTo("서울 성동구");
        assertThat(curationRow.get("type")).isEqualTo("POPUP");

        // then - popup 테이블 확인
        List<Map<String, Object>> popupRows = jdbcTemplate.queryForList(
                "SELECT * FROM popup"
        );
        assertThat(popupRows).hasSize(1);

        Map<String, Object> popupRow = popupRows.get(0);
        assertThat(popupRow.get("city")).isEqualTo("서울");
        assertThat(popupRow.get("district")).isEqualTo("성동구");
        assertThat(popupRow.get("place_name")).isEqualTo("성수동 테스트 장소");
        assertThat(popupRow.get("homepage_url")).isEqualTo("https://example.com");
        assertThat(popupRow.get("sns_url")).isEqualTo("https://instagram.com/test");

        // then - 두 테이블의 id가 동일한지 확인 (JOINED 상속)
        assertThat(curationRow.get("id")).isEqualTo(popupRow.get("id"));
    }

    @Test
    @DisplayName("Popup 엔티티 조회 시 curation과 popup 테이블이 JOIN되어 조회된다")
    void findPopupWithJoinedTables() {
        // given
        PopupRaw raw = PopupRaw.builder()
                .popupId("find-test-id")
                .title("조회 테스트 팝업")
                .thumbnailImageUrl("https://example.com/thumb2.jpg")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 28))
                .city("서울")
                .district("강남구")
                .placeName("강남역 테스트")
                .category(List.of("음식"))
                .tags(List.of("맛집"))
                .isFree(false)
                .reservationRequired(true)
                .homepageUrl("https://gangnam.com")
                .snsUrl(null)
                .reviewStatus(ReviewStatus.APPROVED)
                .build();

        Popup saved = Popup.fromRaw(raw);
        popupRepository.save(saved);

        entityManager.flush();
        entityManager.clear();

        // when
        Popup found = popupRepository.findByPopupId(saved.getPopupId()).orElseThrow();

        // then - Curation 필드 (부모)
        assertThat(found.getTitle()).isEqualTo("조회 테스트 팝업");
        assertThat(found.getThumbnail()).isEqualTo("https://example.com/thumb2.jpg");
        assertThat(found.getRegion()).isEqualTo("서울 강남구");
        assertThat(found.getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(found.getEndDate()).isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(found.getTags()).containsExactly("맛집");
        assertThat(found.getType()).isEqualTo(CurationType.POPUP);

        // then - Popup 필드 (자식)
        assertThat(found.getCity()).isEqualTo("서울");
        assertThat(found.getDistrict()).isEqualTo("강남구");
        assertThat(found.getPlaceName()).isEqualTo("강남역 테스트");
        assertThat(found.getCategory()).containsExactly("음식");
        assertThat(found.isFree()).isFalse();
        assertThat(found.isReservationRequired()).isTrue();
        assertThat(found.getHomepageUrl()).isEqualTo("https://gangnam.com");
        assertThat(found.getSnsUrl()).isNull();
    }

    @Test
    @DisplayName("PopupRaw 승인 후 Popup으로 변환하여 저장하면 상속 구조로 저장된다")
    void approvePopupRawAndSaveAsPopup() {
        // given - PopupRaw 저장
        PopupRaw raw = PopupRaw.builder()
                .popupId("approval-test-id")
                .title("승인 테스트 팝업")
                .thumbnailImageUrl("https://example.com/approval.jpg")
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 3, 31))
                .city("서울")
                .district("마포구")
                .placeName("홍대입구")
                .category(List.of("문화", "예술"))
                .tags(List.of("전시", "아트"))
                .isFree(true)
                .reservationRequired(false)
                .homepageUrl("https://hongdae.com")
                .snsUrl("https://twitter.com/hongdae")
                .reviewStatus(ReviewStatus.PENDING_REVIEW)
                .build();
        popupRawRepository.save(raw);

        entityManager.flush();
        entityManager.clear();

        // when - 승인 처리 시뮬레이션
        PopupRaw savedRaw = popupRawRepository.findByPopupId("approval-test-id").orElseThrow();
        savedRaw.approve();

        Popup popup = Popup.fromRaw(savedRaw);
        popupRepository.save(popup);

        entityManager.flush();
        entityManager.clear();

        // then - 데이터베이스 테이블 확인
        Integer curationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM curation WHERE type = 'POPUP'", Integer.class
        );
        Integer popupCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM popup", Integer.class
        );

        assertThat(curationCount).isEqualTo(1);
        assertThat(popupCount).isEqualTo(1);

        // then - 엔티티 조회 확인
        Popup found = popupRepository.findByPopupId(popup.getPopupId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("승인 테스트 팝업");
        assertThat(found.getType()).isEqualTo(CurationType.POPUP);
    }
}
