package com.example.ticketing.curation.service;

import com.example.ticketing.config.TestcontainersConfiguration;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.CurationViewHistory;
import com.example.ticketing.curation.domain.Exhibition;
import com.example.ticketing.curation.event.CurationViewedEvent;
import com.example.ticketing.curation.repository.CurationViewHistoryRepository;
import com.example.ticketing.curation.repository.ExhibitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 전시 조회 이력 통합 테스트
 * - 전시 상세 조회 시 조회수 증가 확인
 * - 비동기 이벤트를 통한 조회 이력 저장 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ExhibitionViewHistoryTest {

    @Autowired
    private ExhibitionService exhibitionService;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    @Autowired
    private CurationViewHistoryRepository curationViewHistoryRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        curationViewHistoryRepository.deleteAll();
        exhibitionRepository.deleteAll();
    }

    @Test
    @DisplayName("전시 상세 조회 시 조회수가 증가한다")
    void incrementViewCount() {
        // given
        Exhibition exhibition = createTestExhibition();
        exhibitionRepository.save(exhibition);
        Long exhibitionId = exhibition.getId();
        Long initialViewCount = exhibition.getViewCount();

        // when
        exhibitionService.getExhibition(exhibitionId, null);

        // then
        Exhibition updated = exhibitionRepository.findById(exhibitionId).orElseThrow();
        assertThat(updated.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("전시 상세 조회 시 비동기로 조회 이력이 저장된다")
    void saveViewHistoryAsync() {
        // given
        Exhibition exhibition = createTestExhibition();
        exhibitionRepository.save(exhibition);
        Long exhibitionId = exhibition.getId();
        Long userId = 1L;

        // when
        exhibitionService.getExhibition(exhibitionId, userId);

        // then - 비동기 작업 완료 대기 (최대 5초)
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CurationViewHistory> histories = curationViewHistoryRepository.findAll();
                    assertThat(histories).isNotEmpty();

                    CurationViewHistory history = histories.stream()
                            .filter(h -> h.getCurationId().equals(exhibitionId))
                            .findFirst()
                            .orElseThrow();

                    assertThat(history.getCurationId()).isEqualTo(exhibitionId);
                    assertThat(history.getCurationType()).isEqualTo(CurationType.EXHIBITION);
                    assertThat(history.getUserId()).isEqualTo(userId);
                    assertThat(history.getViewedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("비로그인 사용자 조회 시에도 이력이 저장된다 (userId = null)")
    void saveViewHistoryForAnonymousUser() {
        // given
        Exhibition exhibition = createTestExhibition();
        exhibitionRepository.save(exhibition);
        Long exhibitionId = exhibition.getId();

        // when
        exhibitionService.getExhibition(exhibitionId, null);

        // then - 비동기 작업 완료 대기
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CurationViewHistory> histories = curationViewHistoryRepository.findAll();
                    assertThat(histories).isNotEmpty();

                    CurationViewHistory history = histories.stream()
                            .filter(h -> h.getCurationId().equals(exhibitionId))
                            .findFirst()
                            .orElseThrow();

                    assertThat(history.getCurationId()).isEqualTo(exhibitionId);
                    assertThat(history.getUserId()).isNull();
                });
    }

    @Test
    @DisplayName("동일 전시를 여러 번 조회하면 이력이 여러 개 쌓인다")
    void multipleViewsCreateMultipleHistories() {
        // given
        Exhibition exhibition = createTestExhibition();
        exhibitionRepository.save(exhibition);
        Long exhibitionId = exhibition.getId();
        Long userId = 1L;

        // when - 3번 조회
        exhibitionService.getExhibition(exhibitionId, userId);
        exhibitionService.getExhibition(exhibitionId, userId);
        exhibitionService.getExhibition(exhibitionId, userId);

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CurationViewHistory> histories = curationViewHistoryRepository.findAll();
                    long count = histories.stream()
                            .filter(h -> h.getCurationId().equals(exhibitionId))
                            .count();

                    assertThat(count).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("이벤트를 직접 발행하면 조회 이력이 저장된다")
    void publishEventDirectly() {
        // given
        Exhibition exhibition = createTestExhibition();
        exhibitionRepository.save(exhibition);
        Long exhibitionId = exhibition.getId();
        Long userId = 1L;

        CurationViewedEvent event = new CurationViewedEvent(
                exhibitionId,
                CurationType.EXHIBITION,
                userId
        );

        // when
        eventPublisher.publishEvent(event);

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CurationViewHistory> histories = curationViewHistoryRepository.findAll();
                    assertThat(histories).isNotEmpty();

                    CurationViewHistory history = histories.stream()
                            .filter(h -> h.getCurationId().equals(exhibitionId))
                            .findFirst()
                            .orElseThrow();

                    assertThat(history.getCurationId()).isEqualTo(exhibitionId);
                    assertThat(history.getCurationType()).isEqualTo(CurationType.EXHIBITION);
                    assertThat(history.getUserId()).isEqualTo(userId);
                });
    }

    private Exhibition createTestExhibition() {
        return Exhibition.builder()
                .title("테스트 전시")
                .thumbnail("https://example.com/image.jpg")
                .region("서울 강남구")
                .place("테스트 갤러리")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .url("https://example.com")
                .address("서울시 강남구 테스트로 123")
                .description("테스트 전시 설명")
                .image("https://example.com/detail.jpg")
                .build();
    }
}