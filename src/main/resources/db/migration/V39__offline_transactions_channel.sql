-- Channel for reporting: IN_STORE (physical/retail)
ALTER TABLE offline_transactions ADD COLUMN channel VARCHAR(20) NOT NULL DEFAULT 'IN_STORE';
CREATE INDEX idx_offline_transactions_channel ON offline_transactions(channel);
