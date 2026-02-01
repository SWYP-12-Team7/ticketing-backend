package com.example.ticketing.collection.service;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import com.example.ticketing.collection.dto.GeminiPopupData;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.collection.service.PopupDataProcessor.PopupProcessResult;
import com.example.ticketing.config.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PopupDataProcessorTest {

    @Autowired
    private PopupDataProcessor popupDataProcessor;

    @Autowired
    private PopupRawRepository popupRawRepository;

    @Nested
    @DisplayName("processAndSave 메서드")
    class ProcessAndSaveTest {

        @Test
        @DisplayName("성공: 유효한 팝업 데이터가 DB에 저장된다")
        void saveValidPopupData() {
            // given
            GeminiPopupData popupData = new GeminiPopupData(
                    "테스트 팝업스토어",
                    "https://example.com/thumbnail.jpg",
                    "2025-01-01",
                    "2025-01-31",
                    "서울",
                    "성동구",
                    "성수동 테스트 장소",
                    List.of("패션", "뷰티"),
                    "Y",  // isFree
                    "N",  // reservationRequired
                    List.of("인기", "신규"),
                    0.85,  // 높은 신뢰도
                    "https://example.com",  // homepageUrl
                    "https://instagram.com/test"  // snsUrl
            );

            // when
            PopupProcessResult result = popupDataProcessor.processAndSave(List.of(popupData));

            // then
            assertThat(result.savedCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(0);

            List<PopupRaw> savedPopups = popupRawRepository.findAll();
            assertThat(savedPopups).hasSize(1);

            PopupRaw saved = savedPopups.get(0);
            assertThat(saved.getTitle()).isEqualTo("테스트 팝업스토어");
            assertThat(saved.getThumbnailImageUrl()).isEqualTo("https://example.com/thumbnail.jpg");
            assertThat(saved.getCity()).isEqualTo("서울");
            assertThat(saved.getDistrict()).isEqualTo("성동구");
            assertThat(saved.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
        }

        @Test
        @DisplayName("성공: 신뢰도가 낮은 팝업은 PENDING_REVIEW 상태로 저장된다")
        void saveLowConfidencePopupAsPendingReview() {
            // given
            GeminiPopupData popupData = new GeminiPopupData(
                    "신뢰도 낮은 팝업",
                    "https://example.com/thumbnail.jpg",
                    "2025-02-01",
                    "2025-02-28",
                    "서울",
                    "강남구",
                    "강남역 근처",
                    List.of("음식"),
                    "N",
                    "Y",
                    List.of("맛집"),
                    0.6,  // 중간 신뢰도 (0.5 ~ 0.8)
                    null, null
            );

            // when
            PopupProcessResult result = popupDataProcessor.processAndSave(List.of(popupData));

            // then
            assertThat(result.savedCount()).isEqualTo(1);

            List<PopupRaw> savedPopups = popupRawRepository.findAll();
            PopupRaw saved = savedPopups.get(0);
            assertThat(saved.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW);
        }

        @Test
        @DisplayName("실패: 신뢰도가 0.5 미만인 팝업은 저장되지 않는다")
        void skipVeryLowConfidencePopup() {
            // given
            GeminiPopupData popupData = new GeminiPopupData(
                    "신뢰도 매우 낮은 팝업",
                    "https://example.com/thumbnail.jpg",
                    "2025-03-01",
                    "2025-03-31",
                    "서울",
                    "마포구",
                    "홍대입구",
                    List.of("기타"),
                    "Y",
                    "N",
                    List.of(),
                    0.3,  // 낮은 신뢰도
                    null, null
            );

            // when
            PopupProcessResult result = popupDataProcessor.processAndSave(List.of(popupData));

            // then
            assertThat(result.savedCount()).isEqualTo(0);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(result.skippedReasons()).contains("신뢰도 미달: 신뢰도 매우 낮은 팝업");

            List<PopupRaw> savedPopups = popupRawRepository.findAll();
            assertThat(savedPopups).isEmpty();
        }

        @Test
        @DisplayName("실패: 중복 제목의 팝업은 저장되지 않는다")
        void skipDuplicatePopup() {
            // given
            GeminiPopupData firstPopup = new GeminiPopupData(
                    "중복 테스트 팝업",
                    "https://example.com/thumbnail1.jpg",
                    "2025-01-01",
                    "2025-01-31",
                    "서울",
                    "성동구",
                    "성수동",
                    List.of("패션"),
                    "Y",
                    "N",
                    List.of(),
                    0.9,
                    null, null
            );

            GeminiPopupData duplicatePopup = new GeminiPopupData(
                    "중복 테스트 팝업",  // 같은 제목
                    "https://example.com/thumbnail2.jpg",
                    "2025-02-01",
                    "2025-02-28",
                    "서울",
                    "강남구",
                    "강남역",
                    List.of("뷰티"),
                    "N",
                    "Y",
                    List.of(),
                    0.9,
                    null, null
            );

            // when
            popupDataProcessor.processAndSave(List.of(firstPopup));
            PopupProcessResult result = popupDataProcessor.processAndSave(List.of(duplicatePopup));

            // then
            assertThat(result.savedCount()).isEqualTo(0);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(result.skippedReasons()).contains("중복: 중복 테스트 팝업");

            List<PopupRaw> savedPopups = popupRawRepository.findAll();
            assertThat(savedPopups).hasSize(1);
        }

        @Test
        @DisplayName("성공: 썸네일이 없는 팝업은 PENDING_REVIEW 상태로 저장된다")
        void savePopupWithoutThumbnailAsPendingReview() {
            // given
            GeminiPopupData popupData = new GeminiPopupData(
                    "썸네일 없는 팝업",
                    null,  // 썸네일 없음
                    "2025-04-01",
                    "2025-04-30",
                    "서울",
                    "서초구",
                    "서초역",
                    List.of("문화"),
                    "Y",
                    "N",
                    List.of("전시"),
                    0.95,  // 높은 신뢰도지만 썸네일이 없음
                    null, null
            );

            // when
            PopupProcessResult result = popupDataProcessor.processAndSave(List.of(popupData));

            // then
            assertThat(result.savedCount()).isEqualTo(1);

            List<PopupRaw> savedPopups = popupRawRepository.findAll();
            PopupRaw saved = savedPopups.get(0);
            assertThat(saved.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW);
        }

        @Test
        @DisplayName("성공: 여러 팝업 데이터를 한번에 처리한다")
        void processMultiplePopups() {
            // given
            List<GeminiPopupData> popupDataList = List.of(
                    new GeminiPopupData("팝업1", "https://thumb1.jpg", "2025-01-01", "2025-01-31",
                            "서울", "성동구", "성수동", List.of("패션"), "Y", "N", List.of(), 0.9, null, null),
                    new GeminiPopupData("팝업2", "https://thumb2.jpg", "2025-02-01", "2025-02-28",
                            "서울", "강남구", "강남역", List.of("뷰티"), "N", "Y", List.of(), 0.85, null, null),
                    new GeminiPopupData("팝업3", null, "2025-03-01", "2025-03-31",
                            "서울", "마포구", "홍대", List.of("음식"), "Y", "N", List.of(), 0.4, null, null)  // 스킵됨
            );

            // when
            PopupProcessResult result = popupDataProcessor.processAndSave(popupDataList);

            // then
            assertThat(result.savedCount()).isEqualTo(2);
            assertThat(result.skippedCount()).isEqualTo(1);

            List<PopupRaw> savedPopups = popupRawRepository.findAll();
            assertThat(savedPopups).hasSize(2);
        }
    }
}
