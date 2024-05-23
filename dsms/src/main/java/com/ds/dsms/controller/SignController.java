package com.ds.dsms.controller;

import com.ds.dsms.batch.service.BatchSignService;
import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.model.SignedDocument;
import com.ds.dsms.repo.SignedDocumentRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/service")
public class SignController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignController.class);

    private final BatchSignService signService;
    private final SignedDocumentRepository  signedDocumentRepository;

    public SignController(BatchSignService signService, SignedDocumentRepository signedDocumentRepository) {
        this.signService = signService;
        this.signedDocumentRepository = signedDocumentRepository;
    }

    @PostMapping("/sign")
    @ResponseStatus(HttpStatus.ACCEPTED)
//    public Long signDocument(@Validated @RequestBody DocumentPayloadDTO documentPayload) {
    public ResponseEntity<byte[]> signDocument(@Validated @RequestBody DocumentPayloadDTO documentPayload, @RequestHeader("Authorization") String jwtToken) {
        String jobId = documentPayload.getDocumentName() + DigestUtils.sha256Hex(String.valueOf(System.currentTimeMillis()));
//        System.out.println(documentPayload.getDocumentName());
        Long jobIdLong = signService.signJob(jobId, documentPayload, jwtToken);
//        System.out.println("jobIdLong: " + jobIdLong);
        System.out.println("Cache size: " + BatchUtils.getDocumentCache().size());
//        return new ResponseEntity<>(BatchUtils.FINISHED_JOBS.get(jobId), HttpStatus.CREATED);
        Optional<SignedDocument> signedDoc = signedDocumentRepository.findByJobId(jobId);
        if (signedDoc.isPresent()) {
            signedDocumentRepository.deleteByJobId(jobId);
            return new ResponseEntity<>(signedDoc.get().getData(), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.ACCEPTED);


//        return new ResponseEntity<>(signedDocumentRepository.findByJobId(jobId).get().getData(), HttpStatus.ACCEPTED);
//        return jobIdLong;
    }

    @PostMapping("/getDocument")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<byte[]> getDocument(@RequestBody String jobId){
        Optional<SignedDocument> signedDoc = signedDocumentRepository.findByJobId(jobId);
        if (signedDoc.isPresent()) {
            signedDocumentRepository.deleteByJobId(jobId);
            return new ResponseEntity<>(signedDoc.get().getData(), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.CREATED);
    }


}
