-- Rider refresh tokens (for rider auth refresh and logout)
CREATE TABLE rider_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    rider_id BIGINT NOT NULL REFERENCES riders(id) ON DELETE CASCADE,
    token_jti VARCHAR(36) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_rider_refresh_tokens_jti ON rider_refresh_tokens(token_jti);
CREATE INDEX idx_rider_refresh_tokens_rider ON rider_refresh_tokens(rider_id);
CREATE INDEX idx_rider_refresh_tokens_expires ON rider_refresh_tokens(expires_at);
