package com.example.ticketing.curation.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    // Statistics
    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "like_count")
    private int likeCount = 0;

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

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @Builder
    public Popup(String popupId, String title, String thumbnailImageUrl,
                 LocalDate startDate, LocalDate endDate,
                 String city, String district, String placeName,
                 List<String> category, List<String> tags,
                 boolean isFree, boolean reservationRequired,
                 int viewCount, int likeCount) {
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
        this.viewCount = viewCount;
        this.likeCount = likeCount;
    }
}