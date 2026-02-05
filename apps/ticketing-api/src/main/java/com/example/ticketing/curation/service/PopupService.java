package com.example.ticketing.curation.service;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.event.CurationViewedEvent;
import com.example.ticketing.curation.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<Popup> getPopups(String keyword, String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return popupRepository.findByFilters(keyword, city, pageable);
    }

    @Transactional
    public Popup getPopupDetail(String popupId, Long userId) {
        Popup popup = popupRepository.findByPopupId(popupId)
            .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

        popup.incrementViewCount();

        // 비동기로 조회 이력 기록 (메인 스레드 블로킹 방지)
        eventPublisher.publishEvent(new CurationViewedEvent(
            popup.getId(),
            CurationType.POPUP,
            userId
        ));

        return popup;
    }
}
