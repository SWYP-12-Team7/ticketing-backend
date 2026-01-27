package com.example.ticketing.curation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 팝업스토어 엔티티 (Curation 상속)
 */
@Entity
@Table(name = "popup")
@DiscriminatorValue("POPUP")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup extends Curation {

    @Column(name = "is_free")
    private boolean isFree;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> category;

    @Builder
    public Popup(String title, String subTitle, String thumbnail,
                 String region, String place, String address,
                 LocalDate startDate, LocalDate endDate,
                 LocalTime startTime, LocalTime endTime,
                 List<String> tags, String url, String description,
                 String image, ReservationStatus reservationStatus,
                 boolean isFree, List<String> category) {
        super(title, subTitle, thumbnail, region, place, address,
              startDate, endDate, startTime, endTime,
              tags, url, description, image, reservationStatus);
        this.isFree = isFree;
        this.category = category;
    }

    /**
     * PopupRaw 데이터를 기반으로 Popup 생성
     */
    public static Popup fromRaw(com.example.ticketing.collection.domain.PopupRaw raw) {
        ReservationStatus reservationStatus = raw.isReservationRequired()
                ? ReservationStatus.PRE_ORDER
                : ReservationStatus.ON_SITE;

        return Popup.builder()
                .title(raw.getTitle())
                .thumbnail(raw.getThumbnailImageUrl())
                .region(raw.getCity())
                .place(raw.getPlaceName())
                .address(raw.getDistrict())
                .startDate(raw.getStartDate())
                .endDate(raw.getEndDate())
                .tags(raw.getTags())
                .isFree(raw.isFree())
                .category(raw.getCategory())
                .reservationStatus(reservationStatus)
                .build();
    }
}
