package com.securemarts.domain.catalog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * No-op implementation when DigitalOcean Spaces is not configured (e.g. local dev).
 * All store methods return null; uploads are ignored.
 */
@Service
@ConditionalOnMissingBean(S3Client.class)
@Slf4j
public class NoOpFileStorageService implements FileStorageService {

    @Override
    public String store(String storePublicId, MultipartFile file) throws IOException {
        log.warn("Storage not configured (APP_STORAGE_SPACES_* unset); upload ignored");
        return null;
    }

    @Override
    public String storeBusinessDocument(String businessPublicId, MultipartFile file) throws IOException {
        log.warn("Storage not configured; upload ignored");
        return null;
    }

    @Override
    public String storeRiderDocument(String riderPublicId, MultipartFile file) throws IOException {
        log.warn("Storage not configured; upload ignored");
        return null;
    }

    @Override
    public String storePod(String deliveryOrderPublicId, MultipartFile file) throws IOException {
        log.warn("Storage not configured; upload ignored");
        return null;
    }
}
