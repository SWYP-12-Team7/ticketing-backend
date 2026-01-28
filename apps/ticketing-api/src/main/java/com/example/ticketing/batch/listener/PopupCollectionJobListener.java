package com.example.ticketing.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PopupCollectionJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[Batch] 팝업 수집 Job 시작 - JobExecutionId: {}, StartTime: {}",
                jobExecution.getId(), jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("[Batch] 팝업 수집 Job 종료 - JobExecutionId: {}, Status: {}, EndTime: {}",
                jobExecution.getId(), jobExecution.getStatus(), jobExecution.getEndTime());

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("[Batch] 팝업 수집 Job 실패 - ExitDescription: {}",
                    jobExecution.getExitStatus().getExitDescription());
        }
    }
}
