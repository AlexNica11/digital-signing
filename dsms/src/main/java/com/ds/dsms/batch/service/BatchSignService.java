package com.ds.dsms.batch.service;

import com.ds.dsms.auth.jwt.JWTProvider;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.auth.repo.UserRepository;
import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.controller.SignController;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import com.ds.dsms.exception.KeyStoreException;
import com.ds.dsms.exception.UserException;
import com.ds.dsms.repo.KeyStoreRepository;
import com.ds.dsms.repo.PrivateKeyRepository;
import com.ds.dsms.repo.SignedDocumentRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BatchSignService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSignService.class);

    private final JobLauncher jobLauncher;
    private final Job job;
    private final JWTProvider jwtProvider;
    private final UserRepository userRepository;
    private final KeyStoreRepository keyStoreRepository;
    private final PrivateKeyRepository privateKeyRepository;

    @Getter
    private static SignedDocumentRepository signedDocumentRepository = null;

    public BatchSignService(JobLauncher jobLauncher, Job job, JWTProvider jwtProvider, UserRepository userRepository, KeyStoreRepository keyStoreRepository, PrivateKeyRepository privateKeyRepository, SignedDocumentRepository signedDocumentRepository) {
        this.jobLauncher = jobLauncher;
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

        JobParameters jobParameters = new JobParametersBuilder()
                .addString(BatchUtils.BATCH_JOB_ID, jobId, true)
//                .addJobParameter(BatchUtils.BATCH_PAYLOAD, documentPayload, DocumentPayloadDTO.class)
                .toJobParameters();

        addSignaturesToPayload(documentPayload, jwtProvider.getUsernameFromBearerToken(jwtToken));
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

    private void addSignaturesToPayload(DocumentPayloadDTO documentPayload, String username){
        if(Objects.isNull(documentPayload.getKeyStoreParams().getKeyStoreBytes())
                || documentPayload.getKeyStoreParams().getKeyStoreBytes().length == 0){
            User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User: " + username + " not found"));
            KeyStoreParams keyStoreParams = keyStoreRepository.findByKeyStoreNameAndUser(documentPayload.getKeyStoreParams().getKeyStoreName(), user).orElseThrow(() -> new KeyStoreException("Key store not found"));
            documentPayload.getKeyStoreParams().setKeyStoreBytes(keyStoreParams.getKeyStoreBytes());
            Set<PrivateKeyParams> privateKeyParams = new HashSet<>();

            for(PrivateKeyParams privateKeyParam : documentPayload.getKeyStoreParams().getPrivateKeyParams()){
                privateKeyParams.add(privateKeyRepository.findByAliasAndKeyStoreParams(privateKeyParam.getAlias(), keyStoreParams).orElseThrow(() -> new KeyStoreException("Key entry not found")));
            }

            documentPayload.getKeyStoreParams().setPrivateKeyParams(privateKeyParams);
        }
    }
}
