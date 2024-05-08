package com.ds.dsms;

import com.ds.dsms.batch.config.BatchConfiguration;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;

@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = BatchConfiguration.class)
public class DsmsServiceTests {


}
