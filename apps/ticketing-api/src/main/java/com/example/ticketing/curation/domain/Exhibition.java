package com.example.ticketing.curation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exhibition")
@DiscriminatorValue("EXHIBITION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibition extends Curation {

    @Builder
    public Exhibition(String title, String subTitle, String thumbnail,
                      String region, String place,
                      LocalDate startDate, LocalDate endDate,
                      List<String> category, List<String> tags,
                      String url, String address,
                      LocalDateTime startTime, LocalDateTime endTime,
                      String description, String image,
                      ReservationStatus reservationStatus) {
        super(title, subTitle, thumbnail, region, place, startDate, endDate,
              category, tags, url, address, startTime, endTime, description, image,
              reservationStatus, null, null);
    }
}
