package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.curation.domain.ReservationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Popup", description = "관리자 팝업 관리 API")
@Slf4j
@RestController
@RequestMapping("/admin/popups")
@RequiredArgsConstructor
public class AdminPopupController {

    private final PopupRawRepository popupRawRepository;

    @Operation(summary = "팝업 상세 조회", description = "팝업 원본 데이터의 모든 필드를 조회합니다.")
    @GetMapping("/{popupId}")
    public ApiResponse<PopupRawDetailResponse> getPopupDetail(@PathVariable String popupId) {
        PopupRaw raw = popupRawRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        return ApiResponse.success(PopupRawDetailResponse.from(raw));
    }

    @Operation(summary = "팝업 전체 수정", description = "팝업 원본 데이터의 모든 필드를 수정합니다.")
    @PatchMapping("/{popupId}")
    @Transactional
    public ApiResponse<PopupRawDetailResponse> updatePopup(
            @PathVariable String popupId,
            @RequestBody PopupUpdateRequest request
    ) {
        PopupRaw raw = popupRawRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        raw.update(
                request.title(),
                request.subTitle(),
                request.description(),
                request.thumbnailImageUrl(),
                request.startDate(),
                request.endDate(),
                request.city(),
                request.district(),
                request.placeName(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.operatingHours(),
                request.category(),
                request.tags(),
                request.isFree(),
                request.reservationStatus(),
                request.homepageUrl(),
                request.snsUrl()
        );

        log.info("팝업 수정 완료: {}", raw.getTitle());

        return ApiResponse.success(PopupRawDetailResponse.from(raw));
    }

    @Operation(summary = "썸네일만 수정", description = "팝업 썸네일 이미지 URL만 수정합니다.")
    @PatchMapping("/{popupId}/thumbnail")
    @Transactional
    public ApiResponse<String> updateThumbnail(
            @PathVariable String popupId,
            @RequestBody ThumbnailUpdateRequest request
    ) {
        PopupRaw raw = popupRawRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        raw.updateThumbnailImageUrl(request.thumbnailImageUrl());
        log.info("썸네일 업데이트 완료: {} -> {}", raw.getTitle(), request.thumbnailImageUrl());

        return ApiResponse.success("썸네일 업데이트 완료");
    }

    public record ThumbnailUpdateRequest(String thumbnailImageUrl) {}

    public record PopupUpdateRequest(
            String title,
            String subTitle,
            String description,
            String thumbnailImageUrl,
            LocalDate startDate,
            LocalDate endDate,
            String city,
            String district,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Map<String, String> operatingHours,
            List<String> category,
            List<String> tags,
            Boolean isFree,
            ReservationStatus reservationStatus,
            String homepageUrl,
            String snsUrl
    ) {}

    public record PopupRawDetailResponse(
            Long id,
            String popupId,
            String title,
            String subTitle,
            String description,
            String thumbnailImageUrl,
            LocalDate startDate,
            LocalDate endDate,
            String city,
            String district,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Map<String, String> operatingHours,
            List<String> category,
            List<String> tags,
            boolean isFree,
            ReservationStatus reservationStatus,
            String homepageUrl,
            String snsUrl,
            String reviewStatus
    ) {
        public static PopupRawDetailResponse from(PopupRaw raw) {
            return new PopupRawDetailResponse(
                    raw.getId(),
                    raw.getPopupId(),
                    raw.getTitle(),
                    raw.getSubTitle(),
                    raw.getDescription(),
                    raw.getThumbnailImageUrl(),
                    raw.getStartDate(),
                    raw.getEndDate(),
                    raw.getCity(),
                    raw.getDistrict(),
                    raw.getPlaceName(),
                    raw.getAddress(),
                    raw.getLatitude(),
                    raw.getLongitude(),
                    raw.getOperatingHours(),
                    raw.getCategory(),
                    raw.getTags(),
                    raw.isFree(),
                    raw.getReservationStatus(),
                    raw.getHomepageUrl(),
                    raw.getSnsUrl(),
                    raw.getReviewStatus().name()
            );
        }
    }
}
