package com.ds.dsms.batch.config;

import com.ds.dsms.batch.service.BatchUtils;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.DSSAPI;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
import java.util.Objects;

public class DocumentTasklet implements Tasklet, StepExecutionListener {

    private DocumentPayloadDTO documentPayload;
    private byte[] signedDocument;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        documentPayload = (DocumentPayloadDTO) Objects.requireNonNull(stepExecution.getJobParameters().getParameter(BatchUtils.BATCH_JOB_ID)).getValue();
        List<KeyStoreParams> keyStoreParams = documentPayload.getKeyStoreParams();
        for(KeyStoreParams keyStoreParam : keyStoreParams) {
            if(keyStoreParam.getKeyStore() == null){
                keyStoreParam.setKeyStore(BatchUtils.getKeyStore(keyStoreParam.getKeyStoreName()));
            }
        }
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        DSSAPI dssApi = new DSSAPI();
        signedDocument = dssApi.signDocument(documentPayload.getDocument(), documentPayload.getKeyStoreParams(), documentPayload.getSignature(), documentPayload.isExtendSignature());

        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        BatchUtils.FINISHED_JOBS.put(stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID), signedDocument);

        return stepExecution.getExitStatus();
    }
}
