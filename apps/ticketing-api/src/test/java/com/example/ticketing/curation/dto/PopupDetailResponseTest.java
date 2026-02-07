package com.example.ticketing.curation.dto;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.ReservationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PopupDetailResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    @DisplayName("Popup 엔티티의 모든 필드를 PopupDetailResponse로 변환한다")
    void createPopupDetailResponse() throws Exception {
        // given
        Popup popup = createTestPopup();

        // when
        PopupDetailResponse response = PopupDetailResponse.from(popup, false);

        // then
        // Curation 필드 (부모) 확인
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(CurationType.POPUP);
        assertThat(response.title()).isEqualTo("나이키 팝업스토어");
        assertThat(response.subTitle()).isEqualTo("Just Do It - 2024 컬렉션");
        assertThat(response.thumbnail()).isEqualTo("https://example.com/nike-thumbnail.jpg");
        assertThat(response.region()).isEqualTo("서울 성동구");
        assertThat(response.place()).isEqualTo("성수 에스팩토리");
        assertThat(response.description()).isEqualTo("나이키의 최신 2024 컬렉션을 만나보세요.");
        assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.PRE_ORDER);
        assertThat(response.likeCount()).isEqualTo(150L);
        assertThat(response.viewCount()).isEqualTo(1200L);
        assertThat(response.latitude()).isEqualTo(37.5445);
        assertThat(response.longitude()).isEqualTo(127.0567);

        // Popup 필드 (자식 고유) 확인
        assertThat(response.popupId()).isEqualTo("popup-001");
        assertThat(response.city()).isEqualTo("서울");
        assertThat(response.district()).isEqualTo("성동구");
        assertThat(response.placeName()).isEqualTo("성수 에스팩토리");
        assertThat(response.isFree()).isTrue();
        assertThat(response.homepageUrl()).isEqualTo("https://nike.com/popup");
        assertThat(response.snsUrl()).isEqualTo("https://instagram.com/nike");
        assertThat(response.operatingHours()).containsEntry("평일", "10:00-20:00");

        // JSON 출력
        String json = objectMapper.writeValueAsString(response);
        System.out.println("\n========== PopupDetailResponse JSON ==========");
        System.out.println(json);
        System.out.println("===============================================\n");
    }

    private Popup createTestPopup() throws Exception {
        Popup popup = Popup.builder()
                .popupId("popup-001")
                .title("나이키 팝업스토어")
                .subTitle("Just Do It - 2024 컬렉션")
                .thumbnail("https://example.com/nike-thumbnail.jpg")
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 2, 15))
                .region("서울 성동구")
                .place("성수 에스팩토리")
                .category(List.of("패션", "스포츠"))
                .tags(List.of("나이키", "신발", "한정판"))
                .url("https://nike.com/popup")
                .address("서울특별시 성동구 성수이로 51")
                .description("나이키의 최신 2024 컬렉션을 만나보세요.")
                .city("서울")
                .district("성동구")
                .placeName("성수 에스팩토리")
                .latitude(37.5445)
                .longitude(127.0567)
                .isFree(true)
                .reservationStatus(ReservationStatus.PRE_ORDER)
                .homepageUrl("https://nike.com/popup")
                .snsUrl("https://instagram.com/nike")
                .operatingHours(Map.of("평일", "10:00-20:00", "주말", "11:00-19:00"))
                .build();

        // Reflection으로 id, type, likeCount, viewCount 설정
        setField(popup, "id", 1L);
        setField(popup, "type", CurationType.POPUP);
        setField(popup, "likeCount", 150L);
        setField(popup, "viewCount", 1200L);

        return popup;
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = findField(obj.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
