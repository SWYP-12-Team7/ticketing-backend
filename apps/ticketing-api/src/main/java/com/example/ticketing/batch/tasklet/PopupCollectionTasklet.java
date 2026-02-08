package com.example.ticketing.batch.tasklet;

import com.example.ticketing.collection.facade.PopgaCollectionFacade;
import com.example.ticketing.collection.facade.PopgaCollectionFacade.CollectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopupCollectionTasklet implements Tasklet {

    private final PopgaCollectionFacade popgaCollectionFacade;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("[Batch] Popga 팝업 데이터 수집 Tasklet 시작");

        CollectionResult result = popgaCollectionFacade.collectAndSavePopups(0);

        var executionContext = chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext();
        executionContext.put("urlCount", result.urlCount());
        executionContext.put("filteredUrlCount", result.filteredUrlCount());
        executionContext.put("crawledCount", result.crawledCount());
        executionContext.put("savedCount", result.savedCount());
        executionContext.put("skippedCount", result.skippedCount());

        log.info("[Batch] Popga 팝업 데이터 수집 Tasklet 완료 - URL: {}, 신규: {}, 크롤링: {}, 저장: {}, 스킵: {}",
                result.urlCount(), result.filteredUrlCount(), result.crawledCount(),
                result.savedCount(), result.skippedCount());

        return RepeatStatus.FINISHED;
    }
}
