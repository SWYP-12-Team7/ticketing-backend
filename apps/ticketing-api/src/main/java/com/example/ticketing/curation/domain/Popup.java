package com.example.ticketing.curation.domain;

import com.example.ticketing.collection.domain.PopupRaw;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    // Entry info (Popup 고유)
    @Column(name = "is_free")
    private boolean isFree;

    @Column(name = "homepage_url", length = 500)
    private String homepageUrl;

    @Column(name = "sns_url", length = 500)
    private String snsUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "operating_hours")
    private Map<String, String> operatingHours;

    @Builder
    public Popup(String popupId, String title, String subTitle, String thumbnail,
                 LocalDate startDate, LocalDate endDate,
                 String region, String place, List<String> tags,
                 String url, String address, String description,
                 String city, String district, String placeName,
                 List<String> category,
                 boolean isFree, ReservationStatus reservationStatus,
                 String homepageUrl, String snsUrl, Map<String, String> operatingHours) {
        super(title, subTitle, thumbnail, region, place, startDate, endDate,
              category, tags, url, address, null, null, description, null, reservationStatus);
        this.popupId = popupId;
        this.city = city;
        this.district = district;
        this.placeName = placeName;
        this.isFree = isFree;
        this.homepageUrl = homepageUrl;
        this.snsUrl = snsUrl;
        this.operatingHours = operatingHours;
    }

    public static Popup fromRaw(PopupRaw raw) {
        ReservationStatus reservationStatus = raw.getReservationStatus();

        return Popup.builder()
                .popupId(UUID.randomUUID().toString())
                .title(raw.getTitle())
                .subTitle(raw.getSubTitle())
                .thumbnail(raw.getThumbnailImageUrl())
                .startDate(raw.getStartDate())
                .endDate(raw.getEndDate())
                .region(raw.getCity() + " " + raw.getDistrict())
                .place(raw.getPlaceName())
                .tags(raw.getTags())
                .url(raw.getHomepageUrl())
                .address(raw.getAddress())
                .description(raw.getDescription())
                .city(raw.getCity())
                .district(raw.getDistrict())
                .placeName(raw.getPlaceName())
                .category(raw.getCategory())
                .isFree(raw.isFree())
                .reservationStatus(reservationStatus)
                .homepageUrl(raw.getHomepageUrl())
                .snsUrl(raw.getSnsUrl())
                .operatingHours(raw.getOperatingHours())
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
