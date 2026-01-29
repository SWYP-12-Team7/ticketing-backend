package com.example.ticketing.curation.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.example.ticketing.collection.domain.PopupRaw;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "popup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "popup_id", nullable = false, unique = true)
    private String popupId;

    @Column(nullable = false)
    private String title;

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Location
    private String city;
    private String district;

    @Column(name = "place_name")
    private String placeName;

    // Category & Tags
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> category;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    // Entry info
    @Column(name = "is_free")
    private boolean isFree;

    @Column(name = "reservation_required")
    private boolean reservationRequired;

    @Column(name = "homepage_url", length = 500)
    private String homepageUrl;

    @Column(name = "sns_url", length = 500)
    private String snsUrl;

    public static Popup fromRaw(PopupRaw raw) {
        return Popup.builder()
                .popupId(raw.getPopupId())
                .title(raw.getTitle())
                .thumbnailImageUrl(raw.getThumbnailImageUrl())
                .startDate(raw.getStartDate())
                .endDate(raw.getEndDate())
                .city(raw.getCity())
                .district(raw.getDistrict())
                .placeName(raw.getPlaceName())
                .category(raw.getCategory())
                .tags(raw.getTags())
                .isFree(raw.isFree())
                .reservationRequired(raw.isReservationRequired())
                .homepageUrl(raw.getHomepageUrl())
                .snsUrl(raw.getSnsUrl())
                .build();
    }

    public PopupStatus calculateStatus() {
        LocalDate today = LocalDate.now();
        if (startDate == null || endDate == null) {
            return PopupStatus.UPCOMING;
        }
        if (today.isBefore(startDate)) {
            return PopupStatus.UPCOMING;
        } else if (today.isAfter(endDate)) {
            return PopupStatus.ENDED;
        } else {
            return PopupStatus.ONGOING;
        }
    }

    @Builder
    public Popup(String popupId, String title, String thumbnailImageUrl,
                 LocalDate startDate, LocalDate endDate,
                 String city, String district, String placeName,
                 List<String> category, List<String> tags,
                 boolean isFree, boolean reservationRequired,
                 String homepageUrl, String snsUrl) {
        this.popupId = popupId;
        this.title = title;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.district = district;
        this.placeName = placeName;
        this.category = category;
        this.tags = tags;
        this.isFree = isFree;
        this.reservationRequired = reservationRequired;
        this.homepageUrl = homepageUrl;
        this.snsUrl = snsUrl;
    }
}