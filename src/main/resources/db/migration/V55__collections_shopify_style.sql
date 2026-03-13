-- Shopify-style collections: manual vs smart, rules, and collection_products position

-- collections: add type, conditions operator, image
ALTER TABLE collections
    ADD COLUMN IF NOT EXISTS collection_type VARCHAR(20) NOT NULL DEFAULT 'manual',
    ADD COLUMN IF NOT EXISTS conditions_operator VARCHAR(10),
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- collection_rules for smart collection conditions
CREATE TABLE IF NOT EXISTS collection_rules (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    collection_id BIGINT NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    field VARCHAR(50) NOT NULL,
    operator VARCHAR(30) NOT NULL,
    value TEXT,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_collection_rules_collection_id ON collection_rules(collection_id);

-- collection_products: add position and created_at
ALTER TABLE collection_products
    ADD COLUMN IF NOT EXISTS position INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_collection_products_collection_id ON collection_products(collection_id);
