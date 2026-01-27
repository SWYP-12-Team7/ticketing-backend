package com.example.ticketing.curation.facade;

import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.dto.PopupDetailResponse;
import com.example.ticketing.curation.dto.PopupListResponse;
import com.example.ticketing.curation.dto.PopupSummary;
import com.example.ticketing.curation.repository.PopupLikeRepository;
import com.example.ticketing.curation.service.PopupService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Facade Layer - 트랜잭션 외부 I/O 처리
 * 여러 서비스 조합이나 외부 API 호출이 필요한 경우 이 레이어에서 처리
 */
@Component
@RequiredArgsConstructor
public class PopupFacade {

    private final PopupService popupService;
    private final PopupLikeRepository popupLikeRepository;

    public PopupListResponse getPopups(String keyword, String region, int page, int size, Long userId) {
        Page<Popup> popupPage = popupService.getPopups(keyword, region, page, size);

        Set<Long> likedPopupIds = Collections.emptySet();
        if (userId != null) {
            List<Long> popupIds = popupPage.getContent().stream()
                .map(Popup::getId)
                .toList();
            likedPopupIds = popupLikeRepository.findLikedPopupIdsByUserIdAndPopupIds(userId, popupIds);
        }

        Set<Long> finalLikedPopupIds = likedPopupIds;
        List<PopupSummary> popups = popupPage.getContent().stream()
            .map(popup -> PopupSummary.from(popup, finalLikedPopupIds.contains(popup.getId())))
            .toList();

        return PopupListResponse.of(
            popups,
            popupPage.getNumber(),
            popupPage.getSize(),
            popupPage.getTotalElements(),
            popupPage.getTotalPages()
        );
    }

    public PopupDetailResponse getPopupDetail(Long id, Long userId) {
        Popup popup = popupService.getPopupById(id);

        boolean isLiked = false;
        if (userId != null) {
            isLiked = popupLikeRepository.existsByUserIdAndPopupId(userId, popup.getId());
        }

        return PopupDetailResponse.from(popup, isLiked);
    }
}
