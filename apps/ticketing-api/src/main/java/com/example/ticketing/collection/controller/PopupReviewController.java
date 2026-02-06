package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.ReservationStatus;
import com.example.ticketing.curation.repository.PopupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Popup Review", description = "팝업 승인/거절 관리 API")
@Slf4j
@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class PopupReviewController {

    private final PopupRawRepository popupRawRepository;
    private final PopupRepository popupRepository;

    @Operation(summary = "대기 중인 팝업 목록", description = "승인 대기 중인 팝업 목록을 조회합니다.")
    @GetMapping("/pending")
    public ApiResponse<List<PopupReviewResponse>> getPendingPopups() {
        List<PopupRaw> pendingPopups = popupRawRepository.findByReviewStatus(ReviewStatus.PENDING_REVIEW);

        List<PopupReviewResponse> response = pendingPopups.stream()
                .map(PopupReviewResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @Operation(summary = "상태별 팝업 목록", description = "특정 상태의 팝업 목록을 조회합니다.")
    @GetMapping("/popups")
    public ApiResponse<List<PopupReviewResponse>> getPopupsByStatus(@RequestParam ReviewStatus status) {
        List<PopupRaw> popups = popupRawRepository.findByReviewStatus(status);

        List<PopupReviewResponse> response = popups.stream()
                .map(PopupReviewResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @Operation(summary = "팝업 승인", description = "팝업을 승인하고 popup/curation 테이블에 저장합니다.")
    @PostMapping("/{popupId}/approve")
    @Transactional
    public ApiResponse<String> approvePopup(@PathVariable String popupId) {
        PopupRaw raw = popupRawRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        // 1. Raw 데이터 승인 처리
        raw.approve();

        // 2. Popup 테이블로 데이터 이동 (Curation 상속)
        Popup popup = Popup.fromRaw(raw);
        popupRepository.save(popup);

        log.info("팝업 승인 완료: {} -> Popup ID: {}", raw.getTitle(), popup.getId());

        return ApiResponse.success("승인 완료: " + raw.getTitle());
    }

    @Operation(summary = "팝업 거절", description = "팝업을 거절합니다.")
    @PostMapping("/{popupId}/reject")
    @Transactional
    public ApiResponse<String> rejectPopup(@PathVariable String popupId) {
        PopupRaw raw = popupRawRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        raw.reject();
        log.info("팝업 거절 완료: {}", raw.getTitle());

        return ApiResponse.success("거절 완료: " + raw.getTitle());
    }

    public record PopupReviewResponse(
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
            ReviewStatus reviewStatus
    ) {
        public static PopupReviewResponse from(PopupRaw raw) {
            return new PopupReviewResponse(
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
                    raw.getReviewStatus()
            );
        }
    }
}
