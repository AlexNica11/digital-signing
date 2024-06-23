package com.ds.dsms.batch.config;

import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.batch.service.BatchSignService;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.DSSAPI;
import com.ds.dsms.model.SignedDocument;
import com.ds.dsms.repo.SignedDocumentRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.util.Pair;

import java.util.Set;

public class DocumentTasklet implements Tasklet, StepExecutionListener {

    private DocumentPayloadDTO documentPayload;
    private Pair<byte[], String> signedDocument;
    private SignedDocumentRepository signedDocumentRepository;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        documentPayload = BatchUtils.getDocumentFromCache(stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID));
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        DSSAPI dssApi = new DSSAPI();
        signedDocument = dssApi.signDocument(documentPayload.getDocument(), Set.of(documentPayload.getKeyStoreParams()), documentPayload.getSignature(), documentPayload.isExtendSignature());

        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        signedDocumentRepository = BatchSignService.getSignedDocumentRepository();
        signedDocumentRepository.save(new SignedDocument(
                stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID),
                signedDocument.getFirst(),
                stepExecution.getJobParameters().getString(BatchUtils.BATCH_USERNAME),
                documentPayload.getDocumentName().substring(0, documentPayload.getDocumentName().lastIndexOf('.')) + "_signed" + signedDocument.getSecond()));


        return stepExecution.getExitStatus();
    }
}
