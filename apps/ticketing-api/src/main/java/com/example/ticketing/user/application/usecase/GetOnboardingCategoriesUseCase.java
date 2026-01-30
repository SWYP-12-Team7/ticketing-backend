package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.Popup;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.curation.repository.PopupRepository;
import com.example.ticketing.user.application.dto.OnboardingCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOnboardingCategoriesUseCase {

    private final PopupRepository popupRepository;
    private final CurationRepository curationRepository;

    public List<OnboardingCategoryResponse> execute() {
        Map<String, OnboardingCategoryResponse> categoryMap = new LinkedHashMap<>();

        // 팝업에서 카테고리 수집
        for (Popup popup : popupRepository.findAll()) {
            if (popup.getCategory() == null) continue;
            for (String cat : popup.getCategory()) {
                categoryMap.putIfAbsent(cat, new OnboardingCategoryResponse(
                        cat,
                        popup.getThumbnail(),
                        "POPUP"
                ));
            }
        }

        // 전시(Curation)에서 카테고리 수집
        for (Curation curation : curationRepository.findAll()) {
            if (curation.getCategory() == null) continue;
            for (String cat : curation.getCategory()) {
                categoryMap.putIfAbsent(cat, new OnboardingCategoryResponse(
                        cat,
                        curation.getThumbnail(),
                        curation.getType() != null ? curation.getType().name() : "EXHIBITION"
                ));
            }
        }

        return new ArrayList<>(categoryMap.values());
    }
}