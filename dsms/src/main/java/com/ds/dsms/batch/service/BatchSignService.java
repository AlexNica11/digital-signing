package com.ds.dsms.batch.service;

import com.ds.dsms.auth.jwt.JWTProvider;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.auth.repo.UserRepository;
import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.batch.config.BatchConfiguration;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import com.ds.dsms.exception.KeyStoreException;
import com.ds.dsms.exception.UserException;
import com.ds.dsms.model.SignedDocument;
import com.ds.dsms.repo.KeyStoreRepository;
import com.ds.dsms.repo.PrivateKeyRepository;
import com.ds.dsms.repo.SignedDocumentRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BatchSignService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSignService.class);

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job job;
    private final JWTProvider jwtProvider;
    private final UserRepository userRepository;
    private final KeyStoreRepository keyStoreRepository;
    private final PrivateKeyRepository privateKeyRepository;

    @Getter
    private static SignedDocumentRepository signedDocumentRepository = null;

    public BatchSignService(JobLauncher jobLauncher, JobExplorer jobExplorer, Job job, JWTProvider jwtProvider, UserRepository userRepository, KeyStoreRepository keyStoreRepository, PrivateKeyRepository privateKeyRepository, SignedDocumentRepository signedDocumentRepository) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.job = job;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.keyStoreRepository = keyStoreRepository;
        this.privateKeyRepository = privateKeyRepository;
        BatchSignService.signedDocumentRepository = signedDocumentRepository;
    }

    public Long signJob(String jobId, DocumentPayloadDTO documentPayload, String jwtToken){
        if(documentPayload.getKeyStoreParams().getPrivateKeyParams().size() > 1){
            throw new KeyStoreException("Only 1 signature allowed per request");
        }

        String username = jwtProvider.getUsernameFromBearerToken(jwtToken);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(BatchUtils.BATCH_JOB_ID, jobId, true)
                .addString(BatchUtils.BATCH_USERNAME, username, true)
                .addString(BatchUtils.BATCH_DOCUMENT_NAME, documentPayload.getDocumentName(), false)
                .toJobParameters();

        addSignaturesToPayload(documentPayload, username);
        BatchUtils.getDocumentCache().put(jobId, documentPayload);

        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            return jobExecution.getJobId();
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException exception) {
            LOGGER.error("Error running job: {}", jobId, exception);
            return Long.MIN_VALUE;
        }
    }

    private void addSignaturesToPayload(DocumentPayloadDTO documentPayload, String username){
        if(Objects.isNull(documentPayload.getKeyStoreParams().getKeyStoreBytes())
                || documentPayload.getKeyStoreParams().getKeyStoreBytes().length == 0){
            User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User: " + username + " not found"));
            KeyStoreParams keyStoreParams = keyStoreRepository.findByKeyStoreNameAndUser(documentPayload.getKeyStoreParams().getKeyStoreName(), user).orElseThrow(() -> new KeyStoreException("Key store not found"));
            documentPayload.getKeyStoreParams().setKeyStorePassword(keyStoreParams.getKeyStorePassword());
            documentPayload.getKeyStoreParams().setKeyStoreBytes(keyStoreParams.getKeyStoreBytes());
            Set<PrivateKeyParams> privateKeyParams = new HashSet<>();

            for(PrivateKeyParams privateKeyParam : documentPayload.getKeyStoreParams().getPrivateKeyParams()){
                privateKeyParams.add(privateKeyRepository.findByAliasAndKeyStoreParams(privateKeyParam.getAlias(), keyStoreParams).orElseThrow(() -> new KeyStoreException("Key entry not found")));
            }

            documentPayload.getKeyStoreParams().setPrivateKeyParams(privateKeyParams);
        }
    }

    public List<Map<String,String>> getRunningSigningJobs(String jwtToken){
        String username = jwtProvider.getUsernameFromBearerToken(jwtToken);
        Set<JobExecution> jobExecutions = jobExplorer.findRunningJobExecutions(BatchConfiguration.SIGNING_JOB_NAME);
        return getJobsFromJobExecutions(jobExecutions, username);
    }

    public List<Map<String,String>> getCompletedSigningJobs(String jwtToken){
        String username = jwtProvider.getUsernameFromBearerToken(jwtToken);
        List<Map<String,String>> completedJobs = new ArrayList<>();
        List<SignedDocument> signedDocuments = signedDocumentRepository.findByUsername(username);
        for(SignedDocument signedDocument : signedDocuments){
            Map<String, String> jobDetails = new HashMap<>();
            jobDetails.put(BatchUtils.BATCH_JOB_ID, signedDocument.getJobId());
            jobDetails.put(BatchUtils.BATCH_DOCUMENT_NAME, signedDocument.getDocumentName());
            completedJobs.add(jobDetails);
        }

        return completedJobs;
    }

    private List<Map<String,String>> getJobsFromJobExecutions(Collection<JobExecution> jobExecutions, String username){
        List<Map<String,String>> jobs = new ArrayList<>();
        for(JobExecution jobExecution : jobExecutions){
            JobParameters jobParameters = jobExecution.getJobParameters();

            if(username.equals(jobParameters.getString(BatchUtils.BATCH_USERNAME))) {
                Map<String, String> jobDetails = new HashMap<>();
                jobDetails.put(BatchUtils.BATCH_JOB_ID, jobParameters.getString(BatchUtils.BATCH_JOB_ID));
                jobDetails.put(BatchUtils.BATCH_DOCUMENT_NAME, jobParameters.getString(BatchUtils.BATCH_DOCUMENT_NAME));
                jobs.add(jobDetails);
            }
        }

        return jobs;
    }
}
