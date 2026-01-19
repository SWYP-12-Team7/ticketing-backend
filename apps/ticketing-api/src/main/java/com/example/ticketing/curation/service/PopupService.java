package com.example.ticketing.curation.service;

import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public Page<Popup> getPopups(String keyword, String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return popupRepository.findByFilters(keyword, city, pageable);
    }

    @Transactional(readOnly = true)
    public Popup getPopupByPopupId(String popupId) {
        return popupRepository.findByPopupId(popupId)
            .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));
    }

    @Transactional
    public Popup getPopupByPopupIdAndIncrementViewCount(String popupId) {
        Popup popup = popupRepository.findByPopupId(popupId)
            .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));
        popup.incrementViewCount();
        return popup;
    }
}