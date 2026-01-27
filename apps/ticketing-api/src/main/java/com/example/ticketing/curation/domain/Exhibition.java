package com.example.ticketing.curation.domain;

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

/**
 * 전시 엔티티 (Curation 상속)
 */
@Entity
@Table(name = "exhibition")
@DiscriminatorValue("EXHIBITION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibition extends Curation {

    // 전시 고유 필드는 추후 추가

    @Builder
    public Exhibition(String title, String subTitle, String thumbnail,
                      String region, String place, String address,
                      LocalDate startDate, LocalDate endDate,
                      LocalTime startTime, LocalTime endTime,
                      List<String> tags, String url, String description,
                      String image, ReservationStatus reservationStatus) {
        super(title, subTitle, thumbnail, region, place, address,
              startDate, endDate, startTime, endTime,
              tags, url, description, image, reservationStatus);
    }
}
