-- variant_option_values was created without BaseEntity columns (public_id, created_at, updated_at).
-- Add them so Hibernate schema validation passes.

ALTER TABLE variant_option_values
    ADD COLUMN IF NOT EXISTS public_id  VARCHAR(36) NOT NULL DEFAULT gen_random_uuid()::text,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Enforce the uniqueness Hibernate expects on public_id.
-- (PostgreSQL has no ADD CONSTRAINT IF NOT EXISTS; constraint is added once by Flyway.)
ALTER TABLE variant_option_values
    ADD CONSTRAINT uq_variant_option_values_public_id UNIQUE (public_id);
