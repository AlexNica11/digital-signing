package com.ds.dsms.batch;

import com.ds.dsms.controller.dto.DocumentPayloadDTO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BatchUtils {

    private static LoadingCache<String, DocumentPayloadDTO> documentCache;

    public static String BATCH_PAYLOAD = "payload";
    public static String BATCH_JOB_ID = "jobId";

    public static final Map<String, byte[]> FINISHED_JOBS = new ConcurrentHashMap<>();

//    public static final Map<String, DocumentPayloadDTO> UNFINISHED_JOBS = new ConcurrentHashMap<>();

    public static byte[] getKeyStore(String keyStoreName){
        String resourcesPath = "src/test/resources/";
        try {
            return FileUtils.readFileToByteArray(new File(resourcesPath + keyStoreName));
        } catch (Exception exception){
            System.out.println(exception);
            return null;
        }
    }

    public static LoadingCache<String, DocumentPayloadDTO> getDocumentCache(){
        if(Objects.isNull(documentCache)) {
            documentCache = CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, DocumentPayloadDTO>() {
                        @Override
                        public DocumentPayloadDTO load(String key) throws Exception {
                            return null;
                        }
                    });
        }

        return documentCache;
    }

    public static DocumentPayloadDTO getDocumentFromCache(String key){
        try {
            DocumentPayloadDTO document = getDocumentCache().get(key);
            getDocumentCache().invalidate(key);
            return document;
        } catch (ExecutionException exception) {
            throw new RuntimeException(exception);
        }
    }



}
