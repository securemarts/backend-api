package com.shopper.domain.rider.service;

import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.catalog.service.FileStorageService;
import com.shopper.domain.logistics.entity.Rider;
import com.shopper.domain.logistics.entity.RiderDocument;
import com.shopper.domain.logistics.repository.RiderDocumentRepository;
import com.shopper.domain.logistics.repository.RiderRepository;
import com.shopper.domain.rider.dto.RiderDocumentResponse;
import com.shopper.domain.rider.dto.RiderProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiderKycService {

    private final RiderRepository riderRepository;
    private final RiderDocumentRepository riderDocumentRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public RiderProfileResponse getMyProfile(String riderPublicId) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        return RiderProfileResponse.from(rider);
    }

    @Transactional
    public RiderDocumentResponse uploadDocument(String riderPublicId, String documentType, MultipartFile file) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String fileUrl;
        try {
            fileUrl = fileStorageService.storeRiderDocument(riderPublicId, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store document", e);
        }
        if (fileUrl == null) {
            throw new RuntimeException("Failed to store document");
        }
        RiderDocument doc = new RiderDocument();
        doc.setRider(rider);
        doc.setDocumentType(documentType != null ? documentType : "KYC");
        doc.setFileUrl(fileUrl);
        doc.setFileName(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc = riderDocumentRepository.save(doc);
        if (rider.getVerificationStatus() == Rider.VerificationStatus.PENDING) {
            rider.setVerificationStatus(Rider.VerificationStatus.UNDER_REVIEW);
            rider.setRejectionReason(null);
            riderRepository.save(rider);
        }
        return RiderDocumentResponse.from(doc);
    }

    @Transactional(readOnly = true)
    public List<RiderDocumentResponse> listMyDocuments(String riderPublicId) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        return riderDocumentRepository.findByRiderIdOrderByCreatedAtDesc(rider.getId()).stream()
                .map(RiderDocumentResponse::from)
                .collect(Collectors.toList());
    }
}
