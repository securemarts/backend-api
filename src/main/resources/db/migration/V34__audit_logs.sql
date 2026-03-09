-- Audit logs for admin dashboard (Activity ID, User, Action, Module, Timestamp, IP)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    actor_type VARCHAR(20) NOT NULL,
    actor_public_id VARCHAR(36),
    actor_label VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_module_created ON audit_logs(module, created_at DESC);
CREATE INDEX idx_audit_logs_actor_created ON audit_logs(actor_public_id, created_at DESC);

COMMENT ON COLUMN audit_logs.actor_type IS 'ADMIN, MERCHANT, RIDER, SYSTEM';
