package com.example.ticketing.curation.domain;

import com.example.ticketing.collection.domain.PopupRaw;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "popup")
@DiscriminatorValue("POPUP")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup extends Curation {

    @Column(name = "popup_id", nullable = false, unique = true)
    private String popupId;

    // Location (Popup 고유)
    private String city;
    private String district;

    @Column(name = "place_name")
    private String placeName;

    // Category (Popup 고유)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> category;

    // Entry info (Popup 고유)
    @Column(name = "is_free")
    private boolean isFree;

    @Column(name = "reservation_required")
    private boolean reservationRequired;

    @Column(name = "homepage_url", length = 500)
    private String homepageUrl;

    @Column(name = "sns_url", length = 500)
    private String snsUrl;

    @Builder
    public Popup(String popupId, String title, String thumbnail,
                 LocalDate startDate, LocalDate endDate,
                 String region, String place, List<String> tags,
                 String city, String district, String placeName,
                 List<String> category,
                 boolean isFree, boolean reservationRequired,
                 String homepageUrl, String snsUrl) {
        super(title, null, thumbnail, region, place, startDate, endDate, tags,
              null, null, null, null, null, null, null, null, null);
        this.popupId = popupId;
        this.city = city;
        this.district = district;
        this.placeName = placeName;
        this.category = category;
        this.isFree = isFree;
        this.reservationRequired = reservationRequired;
        this.homepageUrl = homepageUrl;
        this.snsUrl = snsUrl;
    }

    public static Popup fromRaw(PopupRaw raw) {
        return Popup.builder()
                .popupId(UUID.randomUUID().toString())
                .title(raw.getTitle())
                .thumbnail(raw.getThumbnailImageUrl())
                .startDate(raw.getStartDate())
                .endDate(raw.getEndDate())
                .region(raw.getCity() + " " + raw.getDistrict())
                .place(raw.getPlaceName())
                .tags(raw.getTags())
                .city(raw.getCity())
                .district(raw.getDistrict())
                .placeName(raw.getPlaceName())
                .category(raw.getCategory())
                .isFree(raw.isFree())
                .reservationRequired(raw.isReservationRequired())
                .homepageUrl(raw.getHomepageUrl())
                .snsUrl(raw.getSnsUrl())
                .build();
    }

    public PopupStatus calculateStatus() {
        LocalDate today = LocalDate.now();
        LocalDate start = getStartDate();
        LocalDate end = getEndDate();

        if (start == null || end == null) {
            return PopupStatus.UPCOMING;
        }
        if (today.isBefore(start)) {
            return PopupStatus.UPCOMING;
        } else if (today.isAfter(end)) {
            return PopupStatus.ENDED;
        } else {
            return PopupStatus.ONGOING;
        }
    }
}
