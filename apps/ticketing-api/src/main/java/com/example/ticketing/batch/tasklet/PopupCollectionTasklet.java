package com.example.ticketing.batch.tasklet;

import com.example.ticketing.collection.facade.PopupCollectionFacade;
import com.example.ticketing.collection.facade.PopupCollectionFacade.CollectionResult;
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

    private final PopupCollectionFacade popupCollectionFacade;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("[Batch] 팝업 데이터 수집 Tasklet 시작");

        CollectionResult result = popupCollectionFacade.collectAndSavePopups();

        chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext()
                .put("collectedCount", result.collectedCount());
        chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext()
                .put("savedCount", result.savedCount());
        chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext()
                .put("skippedCount", result.skippedCount());

        log.info("[Batch] 팝업 데이터 수집 Tasklet 완료 - 수집: {}, 저장: {}, 스킵: {}",
                result.collectedCount(), result.savedCount(), result.skippedCount());

        return RepeatStatus.FINISHED;
    }
}
