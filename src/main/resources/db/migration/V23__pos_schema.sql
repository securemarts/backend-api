-- Offline-first POS: registers, sessions, offline transactions, sync log, cash movements

CREATE TABLE pos_registers (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    device_id VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pos_registers_store ON pos_registers(store_id);

CREATE TABLE pos_sessions (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    register_id BIGINT NOT NULL REFERENCES pos_registers(id) ON DELETE CASCADE,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    opening_cash_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    closing_cash_amount DECIMAL(19, 4),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opened_by VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pos_sessions_register ON pos_sessions(register_id);
CREATE INDEX idx_pos_sessions_status ON pos_sessions(status);

CREATE TABLE offline_transactions (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    register_id BIGINT NOT NULL REFERENCES pos_registers(id) ON DELETE CASCADE,
    session_id BIGINT REFERENCES pos_sessions(id) ON DELETE SET NULL,
    client_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    client_created_at TIMESTAMPTZ,
    synced_at TIMESTAMPTZ,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_offline_tx_register_client ON offline_transactions(register_id, client_id);
CREATE INDEX idx_offline_transactions_store ON offline_transactions(store_id);
CREATE INDEX idx_offline_transactions_session ON offline_transactions(session_id);

CREATE TABLE offline_transaction_items (
    id BIGSERIAL PRIMARY KEY,
    offline_transaction_id BIGINT NOT NULL REFERENCES offline_transactions(id) ON DELETE CASCADE,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_price DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_offline_tx_items_tx ON offline_transaction_items(offline_transaction_id);

CREATE TABLE cash_movements (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    session_id BIGINT NOT NULL REFERENCES pos_sessions(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cash_movements_session ON cash_movements(session_id);

CREATE TABLE sync_logs (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    register_id BIGINT NOT NULL REFERENCES pos_registers(id) ON DELETE CASCADE,
    client_sync_token VARCHAR(100),
    server_sync_token VARCHAR(100),
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    conflict_count INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sync_logs_register ON sync_logs(register_id);
