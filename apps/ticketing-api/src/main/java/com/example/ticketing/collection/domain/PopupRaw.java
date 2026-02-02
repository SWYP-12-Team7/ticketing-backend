package com.example.ticketing.collection.domain;

import com.example.jpa.domain.BaseEntity;
import com.example.ticketing.curation.domain.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 수집된 팝업 원본 데이터를 저장하는 엔티티
 * Gemini API를 통해 수집된 데이터가 이 테이블에 저장됨
 */
@Entity
@Table(name = "popup_raw")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopupRaw extends BaseEntity {

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

    private String city;
    private String district;

    @Column(name = "place_name")
    private String placeName;

    private String address;

    private Double latitude;
    private Double longitude;

    @Column(name = "sub_title")
    private String subTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "operating_hours")
    private Map<String, String> operatingHours;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> category;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    @Column(name = "is_free")
    private boolean isFree;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status")
    private ReservationStatus reservationStatus;

    @Column(name = "homepage_url", length = 500)
    private String homepageUrl;

    @Column(name = "sns_url", length = 500)
    private String snsUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING_REVIEW;

    public void approve() {
        this.reviewStatus = ReviewStatus.APPROVED;
    }

    public void reject() {
        this.reviewStatus = ReviewStatus.REJECTED;
    }

    public void updateThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    @Builder
    public PopupRaw(String popupId, String title, String subTitle,
                    String description, String thumbnailImageUrl,
                    LocalDate startDate, LocalDate endDate,
                    String city, String district, String placeName,
                    String address, Double latitude, Double longitude,
                    Map<String, String> operatingHours,
                    List<String> category, List<String> tags,
                    boolean isFree, ReservationStatus reservationStatus,
                    String homepageUrl, String snsUrl,
                    ReviewStatus reviewStatus) {
        this.popupId = popupId;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.district = district;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingHours = operatingHours;
        this.category = category;
        this.tags = tags;
        this.isFree = isFree;
        this.reservationStatus = reservationStatus;
        this.homepageUrl = homepageUrl;
        this.snsUrl = snsUrl;
        this.reviewStatus = reviewStatus != null ? reviewStatus : ReviewStatus.PENDING_REVIEW;
    }
}
