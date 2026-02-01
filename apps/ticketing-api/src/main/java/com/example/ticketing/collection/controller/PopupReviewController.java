package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.repository.PopupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class PopupReviewController {

    private final PopupRawRepository popupRawRepository;
    private final PopupRepository popupRepository;

    @GetMapping("/pending")
    public ApiResponse<List<PopupReviewResponse>> getPendingPopups() {
        List<PopupRaw> pendingPopups = popupRawRepository.findByReviewStatus(ReviewStatus.PENDING_REVIEW);

        List<PopupReviewResponse> response = pendingPopups.stream()
                .map(PopupReviewResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/popups")
    public ApiResponse<List<PopupReviewResponse>> getPopupsByStatus(@RequestParam ReviewStatus status) {
        List<PopupRaw> popups = popupRawRepository.findByReviewStatus(status);

        List<PopupReviewResponse> response = popups.stream()
                .map(PopupReviewResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @PostMapping("/{popupId}/approve")
    @Transactional
    public ApiResponse<String> approvePopup(@PathVariable String popupId) {
        PopupRaw raw = popupRawRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        // 1. Raw 데이터 승인 처리
        raw.approve();

        // 2. Curation/Popup 테이블로 데이터 이동
        Popup popup = Popup.fromRaw(raw);
        popupRepository.save(popup);

        log.info("팝업 승인 완료: {} -> Curation ID: {}", raw.getTitle(), popup.getId());

        return ApiResponse.success("승인 완료: " + raw.getTitle());
    }

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
            String popupId,
            String title,
            String placeName,
            String startDate,
            String endDate,
            String thumbnailImageUrl,
            ReviewStatus reviewStatus
    ) {
        public static PopupReviewResponse from(PopupRaw raw) {
            return new PopupReviewResponse(
                    raw.getPopupId(),
                    raw.getTitle(),
                    raw.getPlaceName(),
                    raw.getStartDate() != null ? raw.getStartDate().toString() : null,
                    raw.getEndDate() != null ? raw.getEndDate().toString() : null,
                    raw.getThumbnailImageUrl(),
                    raw.getReviewStatus()
            );
        }
    }
}
