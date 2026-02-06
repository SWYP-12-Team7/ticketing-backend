package com.example.ticketing.collection.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * popga.co.kr에서 크롤링한 팝업 데이터를 담는 DTO
 */
@Getter
@Builder
public class PopgaPopupData {
    private String popgaId;
    private String title;
    private String subTitle;
    private String description;
    private String thumbnailImageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String city;
    private String district;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private Map<String, String> operatingHours;
    private List<String> categories;
    private List<String> tags;
    private String homepageUrl;
    private String snsUrl;
    private Boolean isFree;
    private String reservationType;
}
