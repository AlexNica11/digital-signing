package com.ds.dsms.controller;

import com.ds.dsms.batch.service.BatchSignService;
import com.ds.dsms.batch.BatchUtils;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.exception.DSMSException;
import com.ds.dsms.model.SignedDocument;
import com.ds.dsms.repo.SignedDocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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
//        String jobId = documentPayload.getDocumentName() + DigestUtils.sha256Hex(String.valueOf(System.currentTimeMillis()));
        String jobId = documentPayload.getDocumentName() + "_" + documentPayload.getSignature() + "_" + DigestUtils.sha256Hex(String.valueOf(new Random().nextInt(1000)));
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

    @PostMapping("/signWithFormData")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void signDocumentWithFormData(@RequestParam MultipartFile document, @RequestParam String documentName,
                                         @RequestParam String signature, @RequestParam boolean extendSignature,
                                         @RequestParam String keyStoreParams, @RequestHeader("Authorization") String jwtToken) throws IOException {
        KeyStoreParams keyStoreParamsObject = new ObjectMapper().readValue(keyStoreParams, KeyStoreParams.class);
        DocumentPayloadDTO documentPayload = new DocumentPayloadDTO(document.getBytes(), documentName, signature,
                extendSignature, keyStoreParamsObject, false);

//        String jobId = documentPayload.getDocumentName() + DigestUtils.sha256Hex(String.valueOf(System.currentTimeMillis()));
        String jobId = documentPayload.getDocumentName() + "_" + documentPayload.getSignature() + "_" + DigestUtils.sha256Hex(String.valueOf(new Random().nextInt(1000)));
        Long jobIdLong = signService.signJob(jobId, documentPayload, jwtToken);
    }

    @PostMapping("/getDocument")
    public ResponseEntity<byte[]> getDocument(@RequestBody String jobId){
        Optional<SignedDocument> signedDoc = signedDocumentRepository.findByJobId(jobId);
        if (signedDoc.isPresent()) {
            signedDocumentRepository.deleteByJobId(jobId);
            SignedDocument signedDocument = signedDoc.get();
            MediaType mediaType ;
            if(signedDocument.getDocumentName().endsWith(".pdf")){
                mediaType = MediaType.APPLICATION_PDF;
            } else if(signedDocument.getDocumentName().endsWith(".xml")){
                mediaType = MediaType.APPLICATION_XML;
            } else if(signedDocument.getDocumentName().endsWith(".json")){
                mediaType = MediaType.APPLICATION_JSON;
            } else {
                mediaType = MediaType.valueOf("application/pkcs7-mime");
            }
            return ResponseEntity.ok().contentType(mediaType).body(signedDocument.getData());
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.NOT_FOUND);
    }

    @PostMapping("/signingJobs")
    @ResponseStatus(HttpStatus.FOUND)
    public List<Map<String,String>> getSigningJobs(@RequestBody String jobSelection, @RequestHeader("Authorization") String jwtToken){
        return switch (jobSelection) {
            case "running" -> signService.getRunningSigningJobs(jwtToken);
            case "completed" -> signService.getCompletedSigningJobs(jwtToken);
//            case "all" -> signService.getAllJobs(jwtToken);
            default ->
                    throw new DSMSException("Job Selection not supported, please use one of running, completed, all");
        };
    }

    @PostMapping("/runningSigningJobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<Map<String,String>> getRunningSigningJobs(@RequestHeader("Authorization") String jwtToken){
        return signService.getRunningSigningJobs(jwtToken);
    }

    @PostMapping("/completedSigningJobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<Map<String,String>> getCompletedSigningJobs(@RequestHeader("Authorization") String jwtToken){
        return signService.getCompletedSigningJobs(jwtToken);
    }

}
