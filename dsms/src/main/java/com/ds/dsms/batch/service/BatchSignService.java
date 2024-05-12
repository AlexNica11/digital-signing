package com.ds.dsms.batch.service;

import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.controller.SignController;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

@Service
public class BatchSignService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSignService.class);

    private final JobLauncher jobLauncher;
    private final Job job;

    public BatchSignService(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    public Long signJob(String jobId, DocumentPayloadDTO documentPayload){
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(BatchUtils.BATCH_JOB_ID, jobId, true)
//                .addJobParameter(BatchUtils.BATCH_PAYLOAD, documentPayload, DocumentPayloadDTO.class)
                .toJobParameters();

//        BatchUtils.UNFINISHED_JOBS.put(jobId, documentPayload);
        BatchUtils.getDocumentCache().put(jobId, documentPayload);

        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            return jobExecution.getJobId();
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException exception) {
            LOGGER.error("Error running job: {}", jobId, exception);
            return Long.MIN_VALUE;
        }
    }
}
