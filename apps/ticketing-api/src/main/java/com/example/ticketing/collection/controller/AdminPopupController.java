package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/popups")
@RequiredArgsConstructor
public class AdminPopupController {

    private final PopupRawRepository popupRawRepository;

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
}
