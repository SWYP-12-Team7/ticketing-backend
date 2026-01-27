package com.example.ticketing.collection.facade;

import com.example.ticketing.collection.dto.GeminiPopupData;
import com.example.ticketing.collection.service.GeminiPopupCollector;
import com.example.ticketing.collection.service.PopupDataProcessor;
import com.example.ticketing.collection.service.PopupDataProcessor.PopupProcessResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopupCollectionFacade {

    private final GeminiPopupCollector geminiPopupCollector;
    private final PopupDataProcessor popupDataProcessor;

    public CollectionResult collectAndSavePopups() {
        log.info("팝업 데이터 수집 및 저장 시작");

        // 1. Gemini API를 통해 팝업 데이터 수집
        List<GeminiPopupData> collectedData = geminiPopupCollector.collectPopups();

        if (collectedData.isEmpty()) {
            log.warn("수집된 데이터가 없습니다.");
            return new CollectionResult(0, 0, 0, "수집된 데이터 없음");
        }

        // 2. 데이터 정제 및 DB 적재
        PopupProcessResult processResult = popupDataProcessor.processAndSave(collectedData);

        log.info("팝업 데이터 수집 완료. 수집: {}, 저장: {}, 스킵: {}",
                collectedData.size(), processResult.savedCount(), processResult.skippedCount());

        return new CollectionResult(
                collectedData.size(),
                processResult.savedCount(),
                processResult.skippedCount(),
                "성공"
        );
    }

    public record CollectionResult(
            int collectedCount,
            int savedCount,
            int skippedCount,
            String message
    ) {
    }
}