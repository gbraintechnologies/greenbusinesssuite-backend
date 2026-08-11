package com.mesh_suite.service.notify;

import com.mesh_suite.config.S3Properties;
import com.mesh_suite.dao.company.UserCompanyFileRepository;
import com.mesh_suite.domain.company.UserCompanyFile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Transactional
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final UserCompanyFileRepository fileRepository;

    private static final String userFiles = "resources/files/";
    private static final String companyFiles = "resources/documents/";

    // ================= UPLOAD =================

    public String uploadFile(MultipartFile file) {
        try {
            String fileKey = "resources/" + generateUniqueFileName(file.getOriginalFilename());

            putObject(file, fileKey);

            return generateFileUrl(fileKey);

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public String uploadFile(MultipartFile file, Long userId, Long companyId, Long formId) {
        try {
            String fileKey = userFiles + generateUniqueFileName(file.getOriginalFilename());

            putObject(file, fileKey);

            String fileUrl = generateFileUrl(fileKey);

            fileRepository.save(UserCompanyFile.builder()
                    .userId(userId)
                    .companyId(companyId)
                    .formId(formId)
                    .url(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .build());

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public String uploadDocument(MultipartFile file, Long userId, Long companyId, Long formId) {
        try {
            String fileKey = companyFiles + generateUniqueFileName(file.getOriginalFilename());

            putObject(file, fileKey);

            String fileUrl = generateFileUrl(fileKey);

            fileRepository.save(UserCompanyFile.builder()
                    .userId(userId)
                    .companyId(companyId)
                    .formId(formId)
                    .url(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .build());

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private void putObject(MultipartFile file, String key) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
    }

    // ================= FETCH =================

    public List<Map<String, Object>> getUploadedFilesByUserId(Long userId) {
        return fetch(fileRepository.findByUserId(userId), userFiles);
    }

    public List<Map<String, Object>> getUploadedDocsByCompanyId(Long companyId) {
        return fetch(fileRepository.findByCompanyId(companyId), companyFiles);
    }

    public List<Map<String, Object>> getUploadedFilesByUserIdAndFormId(Long userId, Long formId) {
        return fetch(fileRepository.findByUserIdAndFormId(userId, formId), userFiles);
    }

    public List<Map<String, Object>> getUploadedDocsByUserIdAndCompanyId(Long userId, Long companyId) {
        return fetch(fileRepository.findByUserIdAndCompanyId(userId, companyId), companyFiles);
    }

    public List<Map<String, Object>> getIssuedDocsByCompanyIdAndFormIdAndUserId(Long companyId, Long formId, Long userId) {
        return fetch(fileRepository.findByCompanyIdAndFormIdAndUserId(companyId, formId, userId), companyFiles);
    }

    private List<Map<String, Object>> fetch(List<UserCompanyFile> files, String type) {
        return files.stream()
                .filter(f -> f.getUrl() != null && f.getUrl().contains(type))
                .map(f -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", f.getUserId());
                    map.put("formId", f.getFormId());
                    map.put("fileName", f.getFileName());
                    map.put("url", f.getUrl());
                    map.put("createdOn", f.getCreatedOn());
                    return map;
                }).toList();
    }

    // ================= DOWNLOAD =================

    public byte[] downloadFile(String fileUrl) {
        String key = extractKey(fileUrl);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    // ================= DELETE =================

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String key = extractKey(fileUrl);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    // ================= UTIL =================

    private String generateFileUrl(String key) {
        return s3Properties.getBaseUrl() + "/" + key;
    }

    private String extractKey(String url) {
        String base = s3Properties.getBaseUrl();

        if (!url.startsWith(base)) {
            throw new IllegalArgumentException("Invalid URL");
        }

        return url.substring(base.length() + 1);
    }

    private String generateUniqueFileName(String original) {
        String ext = original.substring(original.lastIndexOf("."));
        return UUID.randomUUID() + ext;
    }
}