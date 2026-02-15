-- Business subscription (vendor subscription tiers)
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(20) NOT NULL DEFAULT 'BASIC';
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS current_period_ends_at TIMESTAMPTZ;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS trial_ends_at TIMESTAMPTZ;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS paystack_subscription_code VARCHAR(100);
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS paystack_customer_code VARCHAR(100);

-- Limits per plan (change without redeploy)
CREATE TABLE IF NOT EXISTS subscription_plan_limits (
    id BIGSERIAL PRIMARY KEY,
    plan VARCHAR(20) NOT NULL UNIQUE,
    max_stores INT NOT NULL DEFAULT 1,
    max_locations_per_store INT NOT NULL DEFAULT 1,
    max_products INT NOT NULL DEFAULT 50,
    max_staff INT NOT NULL DEFAULT 1,
    max_price_rules INT NOT NULL DEFAULT 1,
    max_discount_codes INT NOT NULL DEFAULT 1,
    max_pos_registers INT NOT NULL DEFAULT 0,
    delivery_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    paystack_plan_code_monthly VARCHAR(100),
    paystack_plan_code_annual VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscription_plan_limits_plan ON subscription_plan_limits(plan);

-- Seed limits for BASIC, PRO, ENTERPRISE
INSERT INTO subscription_plan_limits (plan, max_stores, max_locations_per_store, max_products, max_staff, max_price_rules, max_discount_codes, max_pos_registers, delivery_enabled)
VALUES
    ('BASIC', 1, 1, 50, 1, 1, 1, 0, FALSE),
    ('PRO', 5, 10, 500, 5, 10, 20, 2, TRUE),
    ('ENTERPRISE', 999, 999, 99999, 999, 999, 999, 999, TRUE)
ON CONFLICT (plan) DO NOTHING;
