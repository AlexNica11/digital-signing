package com.ds.dsms.controller;

import com.ds.dsms.batch.service.BatchSignService;
import com.ds.dsms.batch.service.BatchUtils;
import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service")
public class SignController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignController.class);

    private BatchSignService signService;

    public SignController(BatchSignService signService) {
        this.signService = signService;
    }

    @PostMapping("/sign")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<byte[]> signDocument(@Validated @RequestBody DocumentPayloadDTO documentPayload) {
        String jobId = documentPayload.getDocumentName() + DigestUtils.sha256Hex(String.valueOf(System.currentTimeMillis()));
//        System.out.println(documentPayload.getDocumentName());
        Long jobIdLong = signService.signJob(jobId, documentPayload);
//        System.out.println("jobIdLong: " + jobIdLong);
        return new ResponseEntity<>(BatchUtils.FINISHED_JOBS.get(jobId), HttpStatus.CREATED);
    }


}
