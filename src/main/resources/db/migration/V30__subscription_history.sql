-- Subscription history for record-keeping (append-only). Business table keeps current state for fast querying.
CREATE TABLE IF NOT EXISTS subscription_history (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    plan VARCHAR(20) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    paystack_subscription_code VARCHAR(100),
    period_start TIMESTAMPTZ,
    period_end TIMESTAMPTZ,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscription_history_business_created ON subscription_history(business_id, created_at DESC);
