package com.example.ticketing.collection.facade;

import com.example.ticketing.collection.dto.PopgaPopupData;
import com.example.ticketing.collection.service.PopgaCrawlerService;
import com.example.ticketing.collection.service.PopgaDataProcessor;
import com.example.ticketing.collection.service.PopgaDataProcessor.ProcessResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * popga.co.kr에서 팝업 데이터 수집 워크플로우를 조율하는 Facade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopgaCollectionFacade {

    private final PopgaCrawlerService popgaCrawlerService;
    private final PopgaDataProcessor popgaDataProcessor;

    public record CollectionResult(
            int urlCount,
            int crawledCount,
            int savedCount,
            int skippedCount,
            String message
    ) {}

    /**
     * popga.co.kr에서 팝업 데이터를 수집하고 저장
     *
     * @param limit 수집할 팝업 수 제한 (0이면 전체 수집)
     */
    public CollectionResult collectAndSavePopups(int limit) {
        log.info("Popga 팝업 데이터 수집 시작 (limit: {})", limit);

        try {
            List<String> popupUrls = popgaCrawlerService.collectPopupUrls();
            log.info("팝업 URL {} 개 수집 완료", popupUrls.size());

            if (popupUrls.isEmpty()) {
                return new CollectionResult(0, 0, 0, 0, "팝업 URL이 없습니다.");
            }

            List<PopgaPopupData> crawledData = popgaCrawlerService.crawlPopups(popupUrls, limit);
            log.info("팝업 데이터 {} 개 크롤링 완료", crawledData.size());

            if (crawledData.isEmpty()) {
                return new CollectionResult(popupUrls.size(), 0, 0, 0, "크롤링된 데이터가 없습니다.");
            }

            ProcessResult processResult = popgaDataProcessor.processAndSave(crawledData);

            String message = String.format("성공 (URL: %d, 크롤링: %d, 저장: %d, 스킵: %d)",
                    popupUrls.size(), crawledData.size(), processResult.savedCount(), processResult.skippedCount());

            log.info("Popga 수집 완료: {}", message);

            return new CollectionResult(
                    popupUrls.size(),
                    crawledData.size(),
                    processResult.savedCount(),
                    processResult.skippedCount(),
                    message
            );

        } catch (Exception e) {
            log.error("Popga 수집 중 오류 발생", e);
            return new CollectionResult(0, 0, 0, 0, "오류: " + e.getMessage());
        }
    }
}
