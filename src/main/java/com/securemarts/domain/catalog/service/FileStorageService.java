package com.securemarts.domain.catalog.service;

import com.securemarts.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final UploadProperties uploadProperties;

    /**
     * Stores a file under uploads/{storePublicId}/{uuid}_{sanitizedFilename}.
     * Returns the path segment to use in URL: stores/{storePublicId}/uploads/{storedFilename}.
     */
    public String store(String storePublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "file";
        }
        String sanitized = sanitizeFilename(original);
        String storedName = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_" + sanitized;

        Path base = uploadProperties.getPathAsPath();
        Path storeDir = base.resolve(storePublicId);
        Files.createDirectories(storeDir);
        Path target = storeDir.resolve(storedName);
        file.transferTo(target.toFile());

        return "/api/v1/stores/" + storePublicId + "/uploads/" + storedName;
    }

    /**
     * Stores a compliance document under uploads/businesses/{businessPublicId}/{uuid}_{sanitizedFilename}.
     * Returns the path segment to use in URL: onboarding/businesses/{businessPublicId}/uploads/{storedFilename}.
     */
    public String storeBusinessDocument(String businessPublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "document";
        }
        String sanitized = sanitizeFilename(original);
        String storedName = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_" + sanitized;

        Path base = uploadProperties.getPathAsPath();
        Path businessDir = base.resolve("businesses").resolve(businessPublicId);
        Files.createDirectories(businessDir);
        Path target = businessDir.resolve(storedName);
        file.transferTo(target.toFile());

        return "/api/v1/onboarding/businesses/" + businessPublicId + "/uploads/" + storedName;
    }

    /**
     * Stores rider KYC document under uploads/riders/{riderPublicId}/{uuid}_{sanitizedFilename}.
     */
    public String storeRiderDocument(String riderPublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) original = "document";
        String sanitized = sanitizeFilename(original);
        String storedName = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_" + sanitized;
        Path base = uploadProperties.getPathAsPath();
        Path riderDir = base.resolve("riders").resolve(riderPublicId);
        Files.createDirectories(riderDir);
        Path target = riderDir.resolve(storedName);
        file.transferTo(target.toFile());
        return "/api/v1/rider/kyc/" + riderPublicId + "/uploads/" + storedName;
    }

    public Path resolveRiderDocument(String riderPublicId, String filename) {
        if (riderPublicId == null || filename == null || filename.contains("..") || riderPublicId.contains("..")) return null;
        if (!SAFE_FILENAME.matcher(filename).matches()) return null;
        Path base = uploadProperties.getPathAsPath();
        Path resolved = base.resolve("riders").resolve(riderPublicId).resolve(filename).normalize();
        if (!resolved.startsWith(base)) return null;
        return resolved;
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

    /**
     * Stores proof-of-delivery file under uploads/pod/{deliveryOrderPublicId}/.
     * Returns path segment: /api/v1/rider/deliveries/{deliveryOrderPublicId}/pod/{storedFilename}
     */
    public String storePod(String deliveryOrderPublicId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) original = "pod";
        String sanitized = sanitizeFilename(original);
        String storedName = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_" + sanitized;
        Path base = uploadProperties.getPathAsPath();
        Path podDir = base.resolve("pod").resolve(deliveryOrderPublicId);
        Files.createDirectories(podDir);
        Path target = podDir.resolve(storedName);
        file.transferTo(target.toFile());
        return "/api/v1/rider/deliveries/" + deliveryOrderPublicId + "/pod/" + storedName;
    }

    public Path resolvePodFile(String deliveryOrderPublicId, String filename) {
        if (deliveryOrderPublicId == null || filename == null || filename.contains("..") || deliveryOrderPublicId.contains(".."))
            return null;
        if (!SAFE_FILENAME.matcher(filename).matches()) return null;
        Path base = uploadProperties.getPathAsPath();
        Path resolved = base.resolve("pod").resolve(deliveryOrderPublicId).resolve(filename).normalize();
        if (!resolved.startsWith(base)) return null;
        return resolved;
    }

}
