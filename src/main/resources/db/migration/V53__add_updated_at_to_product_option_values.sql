-- Align product_option_values with JPA BaseEntity (created_at + updated_at)
-- Safe on existing databases: only adds the column if it is missing.

ALTER TABLE product_option_values
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

