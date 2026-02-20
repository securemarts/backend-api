package com.securemarts.domain.catalog.service;

import com.securemarts.config.SpacesStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@ConditionalOnBean(S3Client.class)
@RequiredArgsConstructor
@Slf4j
public class SpacesFileStorageService implements FileStorageService {

    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final S3Client s3Client;
    private final SpacesStorageProperties spacesProperties;

    @Override
    public String store(String storePublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String key = buildKey("products", storePublicId, file.getOriginalFilename());
        upload(key, file);
        return spacesProperties.getPublicUrlForKey(key);
    }

    @Override
    public String storeBusinessDocument(String businessPublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String key = buildKey("compliance", businessPublicId, file.getOriginalFilename());
        upload(key, file);
        return spacesProperties.getPublicUrlForKey(key);
    }

    @Override
    public String storeRiderDocument(String riderPublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String key = buildKey("riders", riderPublicId, file.getOriginalFilename());
        upload(key, file);
        return spacesProperties.getPublicUrlForKey(key);
    }

    @Override
    public String storePod(String deliveryOrderPublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String key = buildKey("pod", deliveryOrderPublicId, file.getOriginalFilename());
        upload(key, file);
        return spacesProperties.getPublicUrlForKey(key);
    }

    private String buildKey(String namespace, String entityId, String originalFilename) {
        String name = originalFilename != null && !originalFilename.isBlank() ? originalFilename : "file";
        String sanitized = sanitizeFilename(name);
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return namespace + "/" + entityId + "/" + unique + "_" + sanitized;
    }

    private void upload(String key, MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(spacesProperties.getBucket())
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private static String sanitizeFilename(String name) {
        int last = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (last >= 0) name = name.substring(last + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (SAFE_FILENAME.matcher(String.valueOf(c)).matches() || c == ' ') {
                sb.append(c == ' ' ? '-' : c);
            }
        }
        if (sb.length() == 0) sb.append("file");
        return sb.toString();
    }
}
