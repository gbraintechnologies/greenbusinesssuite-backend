package com.mesh_suite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class ObjectStorageConfig {

    private final S3Properties props;

    public ObjectStorageConfig(S3Properties props) {
        this.props = props;
    }

    @Bean
    public S3Client s3Client() {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                props.getAccessKey(),
                props.getSecretKey()
        );

        S3Configuration serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)   // REQUIRED for Contabo
                .chunkedEncodingEnabled(false)  //  Important
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(serviceConfig)
                .build();
    }
}