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
                      String charge, String contactPoint) {
        super(title, subTitle, thumbnail, region, place, startDate, endDate,
                null, null,  // category, tags - will be set via applyEnrichment()
                url, address, null, null, description, image, null);
        this.charge = charge;
        this.contactPoint = contactPoint;
    }

    public void applyEnrichment(List<String> category, List<String> tags,
                                java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (category != null && !category.isEmpty()) {
            updateCategory(category);
        }
        if (tags != null && !tags.isEmpty()) {
            updateTags(tags);
        }
        if (startTime != null && endTime != null) {
            updateOperatingHours(startTime, endTime);
        }
    }
}
