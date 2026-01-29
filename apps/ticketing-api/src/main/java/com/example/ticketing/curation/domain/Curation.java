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

    public void 전시상태() {

    }

    public boolean 영업시간여부() {
        // 계산
        return false;
    }


}
