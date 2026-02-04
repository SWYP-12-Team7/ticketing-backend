package com.example.ticketing.mainpage.service;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.curation.repository.PopupRepository;
import com.example.ticketing.mainpage.dto.MainPageResponse;
import com.example.ticketing.mainpage.dto.MainPageResponse.CurationSummary;
import com.example.ticketing.user.domain.KoreanRegion;
import com.example.ticketing.user.domain.UserCategoryPreference;
import com.example.ticketing.user.domain.UserCategoryPreferenceRepository;
import com.example.ticketing.user.domain.UserPreferredRegion;
import com.example.ticketing.user.domain.UserPreferredRegionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MainPageServiceTest {

    @Mock
    private CurationRepository curationRepository;

    @Mock
    private PopupRepository popupRepository;

    @Mock
    private UserPreferredRegionRepository userPreferredRegionRepository;

    @Mock
    private UserCategoryPreferenceRepository userCategoryPreferenceRepository;

    @InjectMocks
    private MainPageService mainPageService;

    @Nested
    @DisplayName("getMainPageData 메서드")
    class GetMainPageData {

        @Test
        @DisplayName("성공: 비로그인 유저는 userCurations가 빈 리스트로 반환된다")
        void returnsEmptyUserCurationsForAnonymousUser() {
            // given
            Long userId = null;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.userCurations()).isEmpty();
            verify(userPreferredRegionRepository, never()).findByUserId(any());
            verify(userCategoryPreferenceRepository, never()).findByUserId(any());
        }

        @Test
        @DisplayName("성공: 로그인 유저의 선호 지역/카테고리 기반 행사가 조회된다")
        void returnsUserCurationsBasedOnPreferences() {
            // given
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            UserPreferredRegion region = UserPreferredRegion.builder()
                    .userId(userId)
                    .region(KoreanRegion.SEOUL)
                    .build();

            UserCategoryPreference category = UserCategoryPreference.builder()
                    .userId(userId)
                    .category("패션")
                    .build();

            Popup userCuration = createPopup(1L, "유저 맞춤 행사", "서울", List.of("패션"));

            given(userPreferredRegionRepository.findByUserId(userId))
                    .willReturn(List.of(region));
            given(userCategoryPreferenceRepository.findByUserId(userId))
                    .willReturn(List.of(category));
            given(curationRepository.findByRegionsAndCategories(List.of("서울"), List.of("패션")))
                    .willReturn(List.of(userCuration));
            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.userCurations()).hasSize(1);
            assertThat(response.userCurations().get(0).title()).isEqualTo("유저 맞춤 행사");
        }

        @Test
        @DisplayName("성공: 선호 지역이 없으면 userCurations가 빈 리스트로 반환된다")
        void returnsEmptyUserCurationsWhenNoPreferredRegions() {
            // given
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            UserCategoryPreference category = UserCategoryPreference.builder()
                    .userId(userId)
                    .category("패션")
                    .build();

            given(userPreferredRegionRepository.findByUserId(userId))
                    .willReturn(Collections.emptyList());
            given(userCategoryPreferenceRepository.findByUserId(userId))
                    .willReturn(List.of(category));
            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.userCurations()).isEmpty();
            verify(curationRepository, never()).findByRegionsAndCategories(anyList(), anyList());
        }

        @Test
        @DisplayName("성공: 선호 카테고리가 없으면 userCurations가 빈 리스트로 반환된다")
        void returnsEmptyUserCurationsWhenNoPreferredCategories() {
            // given
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            UserPreferredRegion region = UserPreferredRegion.builder()
                    .userId(userId)
                    .region(KoreanRegion.SEOUL)
                    .build();

            given(userPreferredRegionRepository.findByUserId(userId))
                    .willReturn(List.of(region));
            given(userCategoryPreferenceRepository.findByUserId(userId))
                    .willReturn(Collections.emptyList());
            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.userCurations()).isEmpty();
            verify(curationRepository, never()).findByRegionsAndCategories(anyList(), anyList());
        }
    }

    @Nested
    @DisplayName("오픈예정 행사 조회")
    class GetUpcomingCurations {

        @Test
        @DisplayName("성공: D-7 이내 오픈예정 행사가 조회된다")
        void returnsUpcomingCurationsWithin7Days() {
            // given
            Long userId = null;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            Popup upcomingCuration = createPopup(2L, "오픈예정 행사", "서울", List.of("뷰티"));

            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(List.of(upcomingCuration));
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.upcomingCurations()).hasSize(1);
            assertThat(response.upcomingCurations().get(0).title()).isEqualTo("오픈예정 행사");
        }
    }

    @Nested
    @DisplayName("무료 행사 조회")
    class GetFreeCurations {

        @Test
        @DisplayName("성공: 무료 행사가 조회된다")
        void returnsFreeCurations() {
            // given
            Long userId = null;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            Popup freePopup = createFreePopup(3L, "무료 행사", "경기", List.of("음식"));

            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(List.of(freePopup));
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.freeCurations()).hasSize(1);
            assertThat(response.freeCurations().get(0).title()).isEqualTo("무료 행사");
        }
    }

    @Nested
    @DisplayName("오늘 오픈 행사 조회")
    class GetTodayOpenCurations {

        @Test
        @DisplayName("성공: 오늘 오픈 행사가 조회된다")
        void returnsTodayOpenCurations() {
            // given
            Long userId = null;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            Popup todayOpenCuration = createPopup(4L, "오늘 오픈 행사", "부산", List.of("문화"));

            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(List.of(todayOpenCuration));

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.todayOpenCurations()).hasSize(1);
            assertThat(response.todayOpenCurations().get(0).title()).isEqualTo("오늘 오픈 행사");
        }
    }

    @Nested
    @DisplayName("CurationSummary 변환")
    class ToCurationSummary {

        @Test
        @DisplayName("성공: Curation이 CurationSummary로 올바르게 변환된다")
        void convertsCurationToSummaryCorrectly() {
            // given
            Long userId = null;
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.plusDays(3);
            LocalDate endDate = today.plusDays(10);
            LocalDate sevenDaysLater = today.plusDays(7);

            Popup curation = Popup.builder()
                    .popupId("popup-123")
                    .title("테스트 행사")
                    .subTitle("테스트 부제목")
                    .thumbnail("https://example.com/thumb.jpg")
                    .region("서울 성동구")
                    .place("성수동")
                    .startDate(startDate)
                    .endDate(endDate)
                    .category(List.of("패션", "뷰티"))
                    .build();

            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(List.of(curation));
            given(popupRepository.findFreePopups())
                    .willReturn(Collections.emptyList());
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.upcomingCurations()).hasSize(1);
            CurationSummary summary = response.upcomingCurations().get(0);
            assertThat(summary.title()).isEqualTo("테스트 행사");
            assertThat(summary.subTitle()).isEqualTo("테스트 부제목");
            assertThat(summary.thumbnail()).isEqualTo("https://example.com/thumb.jpg");
            assertThat(summary.region()).isEqualTo("서울 성동구");
            assertThat(summary.place()).isEqualTo("성수동");
            assertThat(summary.startDate()).isEqualTo(startDate);
            assertThat(summary.endDate()).isEqualTo(endDate);
            assertThat(summary.category()).containsExactly("패션", "뷰티");
            assertThat(summary.dDay()).isEqualTo(3L);
        }

        @Test
        @DisplayName("성공: startDate가 null이면 dDay도 null이다")
        void dDayIsNullWhenStartDateIsNull() {
            // given
            Long userId = null;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            Popup curation = Popup.builder()
                    .popupId("popup-456")
                    .title("날짜 없는 행사")
                    .startDate(null)
                    .endDate(null)
                    .build();

            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(Collections.emptyList());
            given(popupRepository.findFreePopups())
                    .willReturn(List.of(curation));
            given(curationRepository.findByStartDate(today))
                    .willReturn(Collections.emptyList());

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.freeCurations()).hasSize(1);
            assertThat(response.freeCurations().get(0).dDay()).isNull();
        }
    }

    @Nested
    @DisplayName("전체 응답 통합 테스트")
    class FullResponseTest {

        @Test
        @DisplayName("성공: 모든 카테고리의 행사가 함께 반환된다")
        void returnsAllCategoriesTogether() {
            // given
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            UserPreferredRegion region = UserPreferredRegion.builder()
                    .userId(userId)
                    .region(KoreanRegion.SEOUL)
                    .build();

            UserCategoryPreference category = UserCategoryPreference.builder()
                    .userId(userId)
                    .category("패션")
                    .build();

            Popup userCuration = createPopup(1L, "유저 맞춤", "서울", List.of("패션"));
            Popup upcomingCuration = createPopup(2L, "오픈예정", "서울", List.of("뷰티"));
            Popup freeCuration = createFreePopup(3L, "무료", "경기", List.of("음식"));
            Popup todayOpenCuration = createPopup(4L, "오늘오픈", "부산", List.of("문화"));

            given(userPreferredRegionRepository.findByUserId(userId))
                    .willReturn(List.of(region));
            given(userCategoryPreferenceRepository.findByUserId(userId))
                    .willReturn(List.of(category));
            given(curationRepository.findByRegionsAndCategories(List.of("서울"), List.of("패션")))
                    .willReturn(List.of(userCuration));
            given(curationRepository.findUpcomingWithin7Days(today, sevenDaysLater))
                    .willReturn(List.of(upcomingCuration));
            given(popupRepository.findFreePopups())
                    .willReturn(List.of(freeCuration));
            given(curationRepository.findByStartDate(today))
                    .willReturn(List.of(todayOpenCuration));

            // when
            MainPageResponse response = mainPageService.getMainPageData(userId);

            // then
            assertThat(response.userCurations()).hasSize(1);
            assertThat(response.upcomingCurations()).hasSize(1);
            assertThat(response.freeCurations()).hasSize(1);
            assertThat(response.todayOpenCurations()).hasSize(1);

            assertThat(response.userCurations().get(0).title()).isEqualTo("유저 맞춤");
            assertThat(response.upcomingCurations().get(0).title()).isEqualTo("오픈예정");
            assertThat(response.freeCurations().get(0).title()).isEqualTo("무료");
            assertThat(response.todayOpenCurations().get(0).title()).isEqualTo("오늘오픈");
        }
    }

    // Helper methods
    private Popup createPopup(Long id, String title, String region, List<String> category) {
        return Popup.builder()
                .popupId("popup-" + id)
                .title(title)
                .region(region)
                .category(category)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }

    private Popup createFreePopup(Long id, String title, String region, List<String> category) {
        return Popup.builder()
                .popupId("popup-" + id)
                .title(title)
                .region(region)
                .category(category)
                .isFree(true)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }
}
