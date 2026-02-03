package com.example.ticketing.curation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exhibition")
@DiscriminatorValue("EXHIBITION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibition extends Curation {

    @Column(name = "charge")
    private String charge;

    @Column(name = "contact_point")
    private String contactPoint;

    @Builder
    public Exhibition(String title, String subTitle, String thumbnail,
                      String region, String place,
                      LocalDate startDate, LocalDate endDate,
                      String url, String address, String description, String image,
                      String charge, String contactPoint,
                      Double latitude, Double longitude) {
        super(title, subTitle, thumbnail, region, place, startDate, endDate,
                null, null,  // category, tags - will be set via applyEnrichment()
                url, address, null, null, description, image, null, latitude, longitude);
        this.charge = charge;
        this.contactPoint = contactPoint;
    }

    public void applyEnrichment(List<String> category, List<String> tags) {
        if (category != null && !category.isEmpty()) {
            updateCategory(category);
        }
        if (tags != null && !tags.isEmpty()) {
            updateTags(tags);
        }

        // startTime/endTime은 설정하지 않음 - contactPoint에 "운영시간: 페이지 참고" 포함됨
    }
}
