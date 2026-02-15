-- Rider self-onboarding and KYC: verification status, rejection reason, KYC documents

ALTER TABLE riders
    ADD COLUMN verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN rejection_reason VARCHAR(500),
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_riders_verification_status ON riders(verification_status);

-- Rider KYC documents (ID, proof of address, etc.)
CREATE TABLE rider_documents (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    rider_id BIGINT NOT NULL REFERENCES riders(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    mime_type VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rider_documents_rider ON rider_documents(rider_id);
