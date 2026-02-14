-- Business & Store onboarding
CREATE TABLE businesses (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    legal_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    cac_number VARCHAR(50),
    tax_id VARCHAR(50),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_businesses_cac_number ON businesses(cac_number);
CREATE INDEX idx_businesses_tax_id ON businesses(tax_id);

CREATE TABLE business_owners (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    primary_owner BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_business_owners_business_id ON business_owners(business_id);
CREATE INDEX idx_business_owners_user_id ON business_owners(user_id);

CREATE TABLE compliance_documents (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    mime_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_compliance_docs_business_id ON compliance_documents(business_id);

CREATE TABLE business_verifications (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    business_id BIGINT NOT NULL UNIQUE REFERENCES businesses(id) ON DELETE CASCADE,
    verified_by BIGINT,
    verified_at TIMESTAMPTZ,
    notes VARCHAR(1000),
    rejection_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_business_verifications_business_id ON business_verifications(business_id);

CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    domain_slug VARCHAR(100) NOT NULL UNIQUE,
    default_currency VARCHAR(3) DEFAULT 'NGN',
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stores_business_id ON stores(business_id);
CREATE UNIQUE INDEX idx_stores_domain_slug ON stores(domain_slug);

CREATE TABLE store_profiles (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL UNIQUE REFERENCES stores(id) ON DELETE CASCADE,
    logo_url VARCHAR(500),
    description VARCHAR(1000),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(2) DEFAULT 'NG',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_store_profiles_store_id ON store_profiles(store_id);

CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    bank_code VARCHAR(10) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(10) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    payout_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_accounts_store_id ON bank_accounts(store_id);
