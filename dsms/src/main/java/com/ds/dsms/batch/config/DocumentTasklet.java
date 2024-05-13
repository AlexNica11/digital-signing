package com.ds.dsms.batch.config;

import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.batch.service.BatchSignService;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.DSSAPI;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.model.SignedDocument;
import com.ds.dsms.repo.SignedDocumentRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNullApi;

import java.util.List;

public class DocumentTasklet implements Tasklet, StepExecutionListener {

    private DocumentPayloadDTO documentPayload;
    private Pair<byte[], String> signedDocument;
    private SignedDocumentRepository signedDocumentRepository;

    @Override
    public void beforeStep(StepExecution stepExecution) {
//        documentPayload = (DocumentPayloadDTO) Objects.requireNonNull(stepExecution.getJobParameters().getParameter(BatchUtils.BATCH_JOB_ID)).getValue();
        documentPayload = BatchUtils.getDocumentFromCache(stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID));
        List<KeyStoreParams> keyStoreParams = documentPayload.getKeyStoreParams();
        for(KeyStoreParams keyStoreParam : keyStoreParams) {
            if(keyStoreParam.getKeyStore().length == 0){
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
        System.out.println(signedDocument.getFirst().length);
//        BatchUtils.FINISHED_JOBS.put(stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID), signedDocument);

        System.out.println(stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID) + "\n" + signedDocument.getFirst().length + "\n" + documentPayload.getDocumentName().substring(0, documentPayload.getDocumentName().lastIndexOf('.')) + "_signed" + signedDocument.getSecond());
        signedDocumentRepository = BatchSignService.getSignedDocumentRepository();
        signedDocumentRepository.save(new SignedDocument(
                stepExecution.getJobParameters().getString(BatchUtils.BATCH_JOB_ID),
                signedDocument.getFirst(),
                documentPayload.getDocumentName().substring(0, documentPayload.getDocumentName().lastIndexOf('.')) + "_signed" + signedDocument.getSecond()));


        return stepExecution.getExitStatus();
    }
}
