package com.ds.dsms.batch.service;

import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BatchUtils {
    public static String BATCH_PAYLOAD = "payload";
    public static String BATCH_JOB_ID = "jobId";

    public static final Map<String, byte[]> FINISHED_JOBS = new ConcurrentHashMap<>();

    public static final Map<String, DocumentPayloadDTO> UNFINISHED_JOBS = new ConcurrentHashMap<>();

    public static byte[] getKeyStore(String keyStoreName){
        String resourcesPath = "src/test/resources/";
        try {
            return FileUtils.readFileToByteArray(new File(resourcesPath + keyStoreName));
        } catch (Exception exception){
            System.out.println(exception);
            return null;
        }
    }


}
