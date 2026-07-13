package com.mesh_suite.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3Properties {

    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
    private String endpoint;

    private String folderName = "resources";

    public String getBaseUrl() {
        String base = endpoint.replaceAll("/$", "");
        return base + "/" + bucketName;
    }

    public String getKeyPrefix() {
        return folderName.replaceAll("^/|/$", "");
    }
}

