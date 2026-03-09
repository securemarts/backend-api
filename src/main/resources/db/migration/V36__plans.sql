-- Admin-managed plans (display, pricing, features). subscription_plan_limits stays for enforcement.
CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    billing_cycle VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    currency VARCHAR(10) NOT NULL DEFAULT 'NGN',
    price_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_plans_code ON plans(code);
CREATE INDEX idx_plans_status ON plans(status);

-- Plan features (toggles and limits for UI). Enforcement still uses subscription_plan_limits.
CREATE TABLE plan_features (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_key VARCHAR(80) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    limit_value INT,
    UNIQUE(plan_id, feature_key)
);

CREATE INDEX idx_plan_features_plan_id ON plan_features(plan_id);

-- Seed plans for existing BASIC, PRO, ENTERPRISE (align with subscription_plan_limits)
INSERT INTO plans (public_id, name, code, description, billing_cycle, currency, price_amount, status) VALUES
(gen_random_uuid()::text, 'Basic', 'BASIC', 'Single store, core catalog and orders', 'MONTHLY', 'NGN', 0, 'ACTIVE'),
(gen_random_uuid()::text, 'Pro', 'PRO', 'Multiple stores, delivery, POS, more limits', 'MONTHLY', 'NGN', 30000, 'ACTIVE'),
(gen_random_uuid()::text, 'Enterprise', 'ENTERPRISE', 'Unlimited limits, priority support', 'MONTHLY', 'NGN', 100000, 'ACTIVE')
ON CONFLICT (code) DO NOTHING;
