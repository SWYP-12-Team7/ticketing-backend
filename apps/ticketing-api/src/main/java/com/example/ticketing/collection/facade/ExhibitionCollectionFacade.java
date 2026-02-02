package com.example.ticketing.collection.facade;

import com.example.ticketing.collection.dto.ExhibitionApiResponse.Item;
import com.example.ticketing.collection.service.ExhibitionApiClient;
import com.example.ticketing.collection.service.ExhibitionDataProcessor;
import com.example.ticketing.collection.service.ExhibitionDataProcessor.ProcessResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExhibitionCollectionFacade {

    private final ExhibitionApiClient exhibitionApiClient;
    private final ExhibitionDataProcessor exhibitionDataProcessor;

    public CollectionResult collectAndSaveExhibitions() {
        log.info("전시 데이터 수집 및 저장 시작");

        List<Item> collectedData = exhibitionApiClient.fetchExhibitions();

        if (collectedData.isEmpty()) {
            log.warn("수집된 전시 데이터가 없습니다.");
            return new CollectionResult(0, 0, 0, "수집된 데이터 없음");
        }

        ProcessResult processResult = exhibitionDataProcessor.processAndSave(collectedData);

        log.info("전시 데이터 수집 완료. 수집: {}, 저장: {}, 스킵: {}",
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
    ) {}
}