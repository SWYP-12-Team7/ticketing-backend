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
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 팝업/전시 공통 엔티티 (부모 클래스)
 */
@Entity
@Table(name = "curation")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Curation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private CurationType type;

    @Column(nullable = false)
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    private String thumbnail;

    // 지역 (서울, 경기 등)
    private String region;

    // 장소명
    private String place;

    // 상세 주소
    private String address;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status")
    private ReservationStatus reservationStatus;

    protected Curation(String title, String subTitle, String thumbnail,
                       String region, String place, String address,
                       LocalDate startDate, LocalDate endDate,
                       LocalTime startTime, LocalTime endTime,
                       List<String> tags, String url, String description,
                       String image, ReservationStatus reservationStatus) {
        this.title = title;
        this.subTitle = subTitle;
        this.thumbnail = thumbnail;
        this.region = region;
        this.place = place;
        this.address = address;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tags = tags;
        this.url = url;
        this.description = description;
        this.image = image;
        this.reservationStatus = reservationStatus;
    }

    /**
     * 현재 진행 상태 계산
     */
    public CurationStatus calculateStatus() {
        LocalDate today = LocalDate.now();
        if (startDate == null || endDate == null) {
            return CurationStatus.UPCOMING;
        }
        if (today.isBefore(startDate)) {
            return CurationStatus.UPCOMING;
        } else if (today.isAfter(endDate)) {
            return CurationStatus.ENDED;
        } else {
            return CurationStatus.ONGOING;
        }
    }

    /**
     * 현재 운영 중인지 확인
     */
    public boolean isOpen() {
        if (startTime == null || endTime == null) {
            return true;
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
