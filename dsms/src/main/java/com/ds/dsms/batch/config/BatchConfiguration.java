package com.ds.dsms.batch.config;

import com.ds.dsms.model.SignedDocument;
import com.ds.dsms.repo.SignedDocumentRepository;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
public class BatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SignedDocumentRepository signedDocumentRepository;

    public static final String SIGNING_JOB_NAME = "signingJob";
    public static final String SIGNING_STEP_NAME = "signingStep";

    public BatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager, SignedDocumentRepository signedDocumentRepository) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.signedDocumentRepository = signedDocumentRepository;
    }

    @Bean
    public Step signingStep(){
        return new StepBuilder(SIGNING_STEP_NAME, jobRepository)
                .tasklet(new DocumentTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Job signingJob(){
        return new JobBuilder(SIGNING_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(signingStep())
                .build();
    }

    @Bean
    @SneakyThrows
    public JobLauncher getJobLauncher(){
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(4);
        taskExecutor.setVirtualThreads(true);
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void deleteOldSignedDocuments() {
        Date date = new Date(System.currentTimeMillis() - 5 * 3600 * 1000);
        List<SignedDocument> signedDocuments = signedDocumentRepository.findAllByCreationDateBefore(date);
        signedDocumentRepository.deleteAll(signedDocuments);
    }
}
