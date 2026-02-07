package com.example.ticketing.curation.facade;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.dto.PopupDetailResponse;
import com.example.ticketing.curation.dto.PopupListResponse;
import com.example.ticketing.curation.dto.PopupSummary;
import com.example.ticketing.curation.service.PopupService;
import com.example.ticketing.user.domain.UserFavoriteRepository;
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
    private final UserFavoriteRepository userFavoriteRepository;

    public PopupListResponse getPopups(String keyword, String city, int page, int size, Long userId) {
        Page<Popup> popupPage = popupService.getPopups(keyword, city, page, size);

        List<Long> popupIds = popupPage.getContent().stream()
            .map(Popup::getId)
            .toList();

        Set<Long> likedPopupIds = getLikedPopupIds(userId, popupIds);

        List<PopupSummary> popups = popupPage.getContent().stream()
            .map(popup -> PopupSummary.from(popup, likedPopupIds.contains(popup.getId())))
            .toList();

        return PopupListResponse.of(
            popups,
            null,
            popupPage.getNumber(),
            popupPage.getSize(),
            popupPage.getTotalElements(),
            popupPage.getTotalPages()
        );
    }

    public PopupDetailResponse getPopupDetail(String popupId, Long userId) {
        Popup popup = popupService.getPopupDetail(popupId, userId);

        boolean isLiked = userId != null && userFavoriteRepository.existsByUserIdAndCurationIdAndCurationType(
                userId, popup.getId(), CurationType.POPUP);

        return PopupDetailResponse.from(popup, isLiked);
    }

    private Set<Long> getLikedPopupIds(Long userId, List<Long> popupIds) {
        if (userId == null || popupIds.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(
            userFavoriteRepository.findCurationIdsByUserIdAndCurationIdInAndCurationType(
                userId, popupIds, CurationType.POPUP)
        );
    }
}