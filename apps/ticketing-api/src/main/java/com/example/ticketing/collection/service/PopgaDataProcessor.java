package com.example.ticketing.collection.service;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import com.example.ticketing.collection.dto.PopgaPopupData;
import com.example.ticketing.collection.repository.PopupRawRepository;
import com.example.ticketing.curation.domain.ReservationStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * popga에서 수집한 데이터를 PopupRaw 엔티티로 변환하고 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopgaDataProcessor {

    private final PopupRawRepository popupRawRepository;

    public record ProcessResult(int savedCount, int skippedCount) {}

    @Transactional
    public ProcessResult processAndSave(List<PopgaPopupData> popupDataList) {
        int savedCount = 0;
        int skippedCount = 0;

        for (PopgaPopupData data : popupDataList) {
            try {
                String popupId = "popga-" + data.getPopgaId();

                if (popupRawRepository.existsByPopupId(popupId)) {
                    log.debug("중복된 팝업 스킵: {} ({})", data.getTitle(), popupId);
                    skippedCount++;
                    continue;
                }

                if (data.getTitle() == null || data.getTitle().isBlank()) {
                    log.warn("제목 없는 팝업 스킵: popgaId={}", data.getPopgaId());
                    skippedCount++;
                    continue;
                }

                PopupRaw popupRaw = convertToPopupRaw(data, popupId);
                popupRawRepository.save(popupRaw);
                savedCount++;

                log.debug("팝업 저장 완료: {} ({})", data.getTitle(), popupId);

            } catch (Exception e) {
                log.error("팝업 저장 실패: {}", data.getTitle(), e);
                skippedCount++;
            }
        }

        log.info("PopgaDataProcessor 처리 완료 - 저장: {}, 스킵: {}", savedCount, skippedCount);
        return new ProcessResult(savedCount, skippedCount);
    }

    private PopupRaw convertToPopupRaw(PopgaPopupData data, String popupId) {
        ReservationStatus reservationStatus = null;
        if (data.getReservationType() != null) {
            try {
                reservationStatus = ReservationStatus.valueOf(data.getReservationType());
            } catch (IllegalArgumentException e) {
                reservationStatus = ReservationStatus.ALL;
            }
        }

        return PopupRaw.builder()
                .popupId(popupId)
                .title(data.getTitle())
                .subTitle(data.getSubTitle())
                .description(data.getDescription())
                .thumbnailImageUrl(data.getThumbnailImageUrl())
                .startDate(data.getStartDate())
                .endDate(data.getEndDate())
                .city(data.getCity())
                .district(data.getDistrict())
                .placeName(data.getPlaceName())
                .address(data.getAddress())
                .latitude(data.getLatitude())
                .longitude(data.getLongitude())
                .operatingHours(data.getOperatingHours())
                .category(data.getCategories())
                .tags(data.getTags())
                .isFree(data.getIsFree() != null ? data.getIsFree() : true)
                .reservationStatus(reservationStatus != null ? reservationStatus : ReservationStatus.ALL)
                .homepageUrl(data.getHomepageUrl())
                .snsUrl(data.getSnsUrl())
                .reviewStatus(ReviewStatus.PENDING_REVIEW)
                .build();
    }
}
