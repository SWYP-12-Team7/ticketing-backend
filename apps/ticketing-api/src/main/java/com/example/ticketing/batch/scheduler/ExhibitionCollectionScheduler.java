package com.example.ticketing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExhibitionCollectionScheduler {

    private final JobOperator jobOperator;
    @Qualifier("exhibitionCollectionJob")
    private final Job exhibitionCollectionJob;

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    public void runExhibitionCollectionJob() {
        log.info("[Scheduler] 전시 수집 스케줄 실행 시작");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobOperator.start(exhibitionCollectionJob, jobParameters);
            log.info("[Scheduler] 전시 수집 Job 실행 완료 - executionId: {}", jobExecution.getId());
        } catch (Exception e) {
            log.error("[Scheduler] 전시 수집 Job 실행 실패", e);
        }
    }
}