package com.example.ticketing.batch.job;

import com.example.ticketing.batch.listener.PopupCollectionJobListener;
import com.example.ticketing.batch.tasklet.PopupCollectionTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PopupCollectionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PopupCollectionTasklet popupCollectionTasklet;
    private final PopupCollectionJobListener popupCollectionJobListener;

    @Bean
    public Job popupCollectionJob() {
        return new JobBuilder("popupCollectionJob", jobRepository)
                .listener(popupCollectionJobListener)
                .start(popupCollectionStep())
                .build();
    }

    @Bean
    public Step popupCollectionStep() {
        return new StepBuilder("popupCollectionStep", jobRepository)
                .tasklet(popupCollectionTasklet, transactionManager)
                .build();
    }
}
