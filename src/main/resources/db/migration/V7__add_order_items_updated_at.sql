-- Add updated_at to order_items for BaseEntity compatibility
ALTER TABLE order_items ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
