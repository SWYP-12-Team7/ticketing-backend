package com.example.ticketing.curation.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "curation")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private CurationType type;

    private String title;

    private String subTitle;

    private String thumbnail;

    // 지역
    private String region;

    // 장소
    private String place;

    // 시작일자
    private LocalDate startDate;

    // 종료일자
    private LocalDate endDate;

    // 카테고리
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> category;

    // 태그
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    // URL
    private String url;

    // 주소
    private String address;

    // 시작시간
    private LocalDateTime startTime;

    // 종료시간
    private LocalDateTime endTime;

    // 상세정보
    private String description;

    // 이미지
    private String image;

    // 사전예약가능여부
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    // 좋아요 수
    @Column(nullable = false)
    private Long likeCount = 0L;

    // 조회수
    @Column(nullable = false)
    private Long viewCount = 0L;

    // 위도
    private Double latitude;

    // 경도
    private Double longitude;

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

    public void updateCategory(List<String> category) {
        this.category = category;
    }

    public void updateTags(List<String> tags) {
        this.tags = tags;
    }

    public void updateOperatingHours(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void 전시상태() {
    }

    protected Curation(String title, String subTitle, String thumbnail,
                       String region, String place,
                       LocalDate startDate, LocalDate endDate, List<String> tags,
                       String url, String address,
                       LocalDateTime startTime, LocalDateTime endTime,
                       String description, String image,
                       ReservationStatus reservationStatus,
                       Double latitude, Double longitude) {
        this.title = title;
        this.subTitle = subTitle;
        this.thumbnail = thumbnail;
        this.region = region;
        this.place = place;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = tags;
        this.url = url;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.image = image;
        this.reservationStatus = reservationStatus;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean 영업시간여부() {
        // 계산
        return false;
    }

}
