package com.example.ticketing.collection.controller;

import com.example.ticketing.common.response.ApiResponse;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.domain.ReviewStatus;
import com.example.ticketing.curation.repository.PopupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class PopupReviewController {

    private final PopupRepository popupRepository;

    @GetMapping("/pending")
    public ApiResponse<List<PopupReviewResponse>> getPendingPopups() {
        List<Popup> pendingPopups = popupRepository.findByReviewStatus(ReviewStatus.PENDING_REVIEW);

        List<PopupReviewResponse> response = pendingPopups.stream()
                .map(PopupReviewResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/popups")
    public ApiResponse<List<PopupReviewResponse>> getPopupsByStatus(@RequestParam ReviewStatus status) {
        List<Popup> popups = popupRepository.findByReviewStatus(status);

        List<PopupReviewResponse> response = popups.stream()
                .map(PopupReviewResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @PostMapping("/{popupId}/approve")
    @Transactional
    public ApiResponse<String> approvePopup(@PathVariable String popupId) {
        Popup popup = popupRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        popup.approve();
        log.info("팝업 승인 완료: {}", popup.getTitle());

        return ApiResponse.success("승인 완료: " + popup.getTitle());
    }

    @PostMapping("/{popupId}/reject")
    @Transactional
    public ApiResponse<String> rejectPopup(@PathVariable String popupId) {
        Popup popup = popupRepository.findByPopupId(popupId)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        popup.reject();
        log.info("팝업 거절 완료: {}", popup.getTitle());

        return ApiResponse.success("거절 완료: " + popup.getTitle());
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
        public static PopupReviewResponse from(Popup popup) {
            return new PopupReviewResponse(
                    popup.getPopupId(),
                    popup.getTitle(),
                    popup.getPlaceName(),
                    popup.getStartDate() != null ? popup.getStartDate().toString() : null,
                    popup.getEndDate() != null ? popup.getEndDate().toString() : null,
                    popup.getThumbnailImageUrl(),
                    popup.getReviewStatus()
            );
        }
    }
}
