package com.example.ticketing.collection.service;

import com.example.ticketing.collection.domain.PopupRaw;
import com.example.ticketing.collection.domain.ReviewStatus;
import com.example.ticketing.collection.dto.GeminiPopupData;
import com.example.ticketing.collection.repository.PopupRawRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupDataProcessor {

    private final PopupRawRepository popupRawRepository;

    private static final double HIGH_CONFIDENCE = 0.8;
    private static final double LOW_CONFIDENCE = 0.5;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public PopupProcessResult processAndSave(List<GeminiPopupData> collectedData) {
        log.info("수집된 데이터 처리 시작. 총 {} 건", collectedData.size());

        List<PopupRaw> savedPopups = new ArrayList<>();
        List<String> skippedReasons = new ArrayList<>();

        for (GeminiPopupData data : collectedData) {
            try {
                ProcessResult result = processOne(data);
                if (result.success()) {
                    savedPopups.add(result.popup());
                } else {
                    skippedReasons.add(result.reason());
                }
            } catch (Exception e) {
                log.warn("팝업 데이터 처리 중 오류: {}", data.title(), e);
                skippedReasons.add("처리 오류: " + data.title());
            }
        }

        log.info("처리 완료. 저장: {} 건, 스킵: {} 건", savedPopups.size(), skippedReasons.size());
        return new PopupProcessResult(savedPopups.size(), skippedReasons.size(), skippedReasons);
    }

    private ProcessResult processOne(GeminiPopupData data) {
        double confidence = data.confidence() != null ? data.confidence() : 0.0;

        // 1. 신뢰도 검증 - 0.5 미만은 스킵
        if (confidence < LOW_CONFIDENCE) {
            log.info("[스킵] {} (신뢰도: {})", data.title(), confidence);
            return ProcessResult.skipped("신뢰도 미달: " + data.title());
        }

        // 2. 필수 필드 검증
        if (data.title() == null || data.title().isBlank()) {
            return ProcessResult.skipped("제목 없음");
        }

        // 3. 중복 검사
        if (popupRawRepository.existsByTitle(data.title())) {
            return ProcessResult.skipped("중복: " + data.title());
        }

        // 4. 날짜 파싱
        LocalDate startDate = parseDate(data.startDate());
        LocalDate endDate = parseDate(data.endDate());

        // 5. 상태 결정: 썸네일이 없거나 신뢰도가 낮으면 PENDING_REVIEW
        String thumbnailImageUrl = data.thumbnailImageUrl();
        boolean hasThumbnail = thumbnailImageUrl != null && !thumbnailImageUrl.isBlank();

        ReviewStatus reviewStatus;
        if (!hasThumbnail) {
            reviewStatus = ReviewStatus.PENDING_REVIEW;
            log.info("[썸네일 없음] {} - 관리자 승인 필요", data.title());
        } else if (confidence >= HIGH_CONFIDENCE) {
            reviewStatus = ReviewStatus.APPROVED;
        } else {
            reviewStatus = ReviewStatus.PENDING_REVIEW;
        }

        // 6. 엔티티 생성 및 저장
        PopupRaw popup = PopupRaw.builder()
                .popupId(UUID.randomUUID().toString())
                .title(data.title())
                .thumbnailImageUrl(data.thumbnailImageUrl())
                .startDate(startDate)
                .endDate(endDate)
                .city(data.city() != null ? data.city() : "서울")
                .district(data.district() != null ? data.district() : "성동구")
                .placeName(data.placeName())
                .category(data.categories())
                .tags(data.tags())
                .isFree(data.isFreeBoolean())
                .reservationRequired(data.isReservationRequiredBoolean())
                .reviewStatus(reviewStatus)
                .build();

        PopupRaw saved = popupRawRepository.save(popup);
        log.info("[{}] {} (신뢰도: {})", reviewStatus, saved.getTitle(), confidence);

        return ProcessResult.success(saved);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }

    private record ProcessResult(boolean success, PopupRaw popup, String reason) {
        static ProcessResult success(PopupRaw popup) {
            return new ProcessResult(true, popup, null);
        }

        static ProcessResult skipped(String reason) {
            return new ProcessResult(false, null, reason);
        }
    }

    public record PopupProcessResult(
            int savedCount,
            int skippedCount,
            List<String> skippedReasons
    ) {
    }
}
