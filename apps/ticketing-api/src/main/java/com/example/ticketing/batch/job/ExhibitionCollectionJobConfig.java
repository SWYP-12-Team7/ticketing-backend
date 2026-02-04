package com.example.ticketing.batch.job;

import com.example.ticketing.batch.listener.ExhibitionCollectionJobListener;
import com.example.ticketing.batch.tasklet.ExhibitionCollectionTasklet;
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
public class ExhibitionCollectionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ExhibitionCollectionTasklet exhibitionCollectionTasklet;
    private final ExhibitionCollectionJobListener exhibitionCollectionJobListener;

    @Bean
    public Job exhibitionCollectionJob() {
        return new JobBuilder("exhibitionCollectionJob", jobRepository)
                .listener(exhibitionCollectionJobListener)
                .start(exhibitionCollectionStep())
                .build();
    }

    @Bean
    public Step exhibitionCollectionStep() {
        return new StepBuilder("exhibitionCollectionStep", jobRepository)
                .tasklet(exhibitionCollectionTasklet, transactionManager)
                .build();
    }
}