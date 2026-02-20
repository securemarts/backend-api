package com.securemarts.domain.catalog.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction for storing files (e.g. DigitalOcean Spaces or no-op when not configured).
 */
public interface FileStorageService {

    String store(String storePublicId, MultipartFile file) throws IOException;

    String storeBusinessDocument(String businessPublicId, MultipartFile file) throws IOException;

    String storeRiderDocument(String riderPublicId, MultipartFile file) throws IOException;

    String storePod(String deliveryOrderPublicId, MultipartFile file) throws IOException;
}
