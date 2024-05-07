package com.ds.dsms.controller;

import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service")
public class SignController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignController.class);

    @PostMapping("/sign")
    @ResponseStatus(HttpStatus.CREATED)
    public void signDocument(@Validated DocumentPayloadDTO documentPayload){

    }




}
