package com.securemarts.domain.logistics.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rider_documents", indexes = {
        @Index(name = "idx_rider_documents_rider", columnList = "rider_id")
})
@Getter
@Setter
public class RiderDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "mime_type", length = 50)
    private String mimeType;
}
