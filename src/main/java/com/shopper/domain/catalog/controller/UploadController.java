package com.shopper.domain.catalog.controller;

import com.shopper.domain.catalog.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/stores/{storePublicId}/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{filename}")
    public ResponseEntity<InputStreamResource> serve(
            @PathVariable String storePublicId,
            @PathVariable String filename) throws Exception {
        Path file = fileStorageService.resolveFile(storePublicId, filename);
        if (file == null || !Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(new InputStreamResource(Files.newInputStream(file)));
    }
}
