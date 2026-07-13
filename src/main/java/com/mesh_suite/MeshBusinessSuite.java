package com.mesh_suite;

import com.mesh_suite.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(S3Properties.class)
@EnableTransactionManagement
@EntityScan("com.mesh_suite.domain")
@EnableAsync
public class MeshBusinessSuite {
    public static void main(String[] args) {
       SpringApplication.run(MeshBusinessSuite.class, args);
    }

}