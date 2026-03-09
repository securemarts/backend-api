-- Add updated_at to invoice_payments (entity extends BaseEntity which has updatedAt)
ALTER TABLE invoice_payments
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
