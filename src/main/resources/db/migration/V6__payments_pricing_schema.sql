-- Payments
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    gateway VARCHAR(50) NOT NULL,
    gateway_reference VARCHAR(255),
    gateway_response TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_transactions_store_id ON payment_transactions(store_id);
CREATE INDEX idx_payment_transactions_order_id ON payment_transactions(order_id);
CREATE INDEX idx_payment_transactions_gateway_ref ON payment_transactions(gateway_reference);

CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    payment_transaction_id BIGINT NOT NULL REFERENCES payment_transactions(id) ON DELETE RESTRICT,
    amount DECIMAL(19,4) NOT NULL,
    reason VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_transaction_id);

CREATE TABLE payouts (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    bank_account_id BIGINT REFERENCES bank_accounts(id) ON DELETE SET NULL,
    gateway_reference VARCHAR(255),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payouts_store_id ON payouts(store_id);

-- Pricing & Promotions
CREATE TABLE price_rules (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    value_type VARCHAR(20) NOT NULL,
    value_amount DECIMAL(19,4),
    value_percent DECIMAL(5,2),
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    usage_limit INT,
    usage_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_price_rules_store_id ON price_rules(store_id);

CREATE TABLE discount_codes (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    price_rule_id BIGINT NOT NULL REFERENCES price_rules(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    usage_limit INT,
    usage_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_discount_codes_store_code ON discount_codes(price_rule_id, code);

-- Add updated_at to inventory_movements for BaseEntity compatibility
ALTER TABLE inventory_movements ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL;
