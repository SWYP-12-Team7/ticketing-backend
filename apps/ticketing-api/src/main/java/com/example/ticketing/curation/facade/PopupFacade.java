package com.example.ticketing.curation.facade;

import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.dto.PopupDetailResponse;
import com.example.ticketing.curation.dto.PopupListResponse;
import com.example.ticketing.curation.dto.PopupSummary;
import com.example.ticketing.curation.service.PopupService;
import java.util.List;
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

    public PopupListResponse getPopups(String keyword, String city, int page, int size) {
        Page<Popup> popupPage = popupService.getPopups(keyword, city, page, size);

        List<PopupSummary> popups = popupPage.getContent().stream()
            .map(PopupSummary::from)
            .toList();

        // TODO: 로그인 사용자의 좋아요 목록 조회 (현재는 null)
        List<String> likedPopupIds = null;

        return PopupListResponse.of(
            popups,
            likedPopupIds,
            popupPage.getNumber(),
            popupPage.getSize(),
            popupPage.getTotalElements(),
            popupPage.getTotalPages()
        );
    }

    public PopupDetailResponse getPopupDetail(String popupId) {
        Popup popup = popupService.getPopupByPopupId(popupId);

        // TODO: 로그인 사용자의 좋아요 목록 조회 (현재는 null)
        List<String> likedPopupIds = null;

        return PopupDetailResponse.from(popup, likedPopupIds);
    }
}