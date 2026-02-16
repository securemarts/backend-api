-- Email verification OTP for users and riders (single table, target_type discriminator)
CREATE TABLE email_verification_otps (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(64) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_email_verification_otp_email_type ON email_verification_otps (LOWER(email), target_type);
CREATE INDEX idx_email_verification_otp_expires ON email_verification_otps (expires_at);
