package com.example.ticketing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PopupCollectionScheduler {

    private final JobOperator jobOperator;
    private final Job popupCollectionJob;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void runPopupCollectionJob() {
        log.info("[Scheduler] 팝업 수집 스케줄 실행 시작");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobOperator.start(popupCollectionJob, jobParameters);
            log.info("[Scheduler] 팝업 수집 Job 실행 완료 - executionId: {}", jobExecution.getId());
        } catch (Exception e) {
            log.error("[Scheduler] 팝업 수집 Job 실행 실패", e);
        }
    }
}
