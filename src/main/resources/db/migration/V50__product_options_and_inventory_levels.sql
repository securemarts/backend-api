-- Product options (name + values) and variant-option mapping
CREATE TABLE product_options (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    position INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_product_options_product_id ON product_options(product_id);

CREATE TABLE product_option_values (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    option_id BIGINT NOT NULL REFERENCES product_options(id) ON DELETE CASCADE,
    value VARCHAR(100) NOT NULL,
    position INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_product_option_values_option_id ON product_option_values(option_id);

CREATE TABLE variant_option_values (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    option_value_id BIGINT NOT NULL REFERENCES product_option_values(id) ON DELETE CASCADE,
    UNIQUE(variant_id, option_value_id)
);
CREATE INDEX idx_variant_option_values_variant ON variant_option_values(variant_id);
CREATE INDEX idx_variant_option_values_option_value ON variant_option_values(option_value_id);

-- Products: add vendor, product_type, published_at
ALTER TABLE products ADD COLUMN vendor VARCHAR(255);
ALTER TABLE products ADD COLUMN product_type VARCHAR(255);
ALTER TABLE products ADD COLUMN published_at TIMESTAMPTZ;

-- Product variants: add barcode, cost, weight, track_inventory, requires_shipping
ALTER TABLE product_variants ADD COLUMN barcode VARCHAR(100);
ALTER TABLE product_variants ADD COLUMN cost_amount DECIMAL(12,2);
ALTER TABLE product_variants ADD COLUMN weight DECIMAL(10,3);
ALTER TABLE product_variants ADD COLUMN weight_unit VARCHAR(10);
ALTER TABLE product_variants ADD COLUMN track_inventory BOOLEAN DEFAULT TRUE;
ALTER TABLE product_variants ADD COLUMN requires_shipping BOOLEAN DEFAULT TRUE;

-- Collection-products many-to-many (before dropping product.collection_id)
CREATE TABLE collection_products (
    collection_id BIGINT NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (collection_id, product_id)
);
CREATE INDEX idx_collection_products_product ON collection_products(product_id);

-- Collections: move from business_id to store_id
ALTER TABLE collections ADD COLUMN store_id BIGINT REFERENCES stores(id) ON DELETE CASCADE;
UPDATE collections c SET store_id = (SELECT id FROM stores s WHERE s.business_id = c.business_id ORDER BY s.id LIMIT 1);
ALTER TABLE collections ALTER COLUMN store_id SET NOT NULL;
CREATE INDEX idx_collections_store_id ON collections(store_id);
ALTER TABLE collections DROP CONSTRAINT IF EXISTS collections_business_id_fkey;
DROP INDEX IF EXISTS idx_collections_business_id;
ALTER TABLE collections DROP COLUMN business_id;

-- Migrate product.collection_id into collection_products
INSERT INTO collection_products (collection_id, product_id)
SELECT collection_id, id FROM products WHERE collection_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Products: drop collection_id
DROP INDEX IF EXISTS idx_products_collection_id;
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_collection_id_fkey;
ALTER TABLE products DROP COLUMN collection_id;

-- Inventory restructure: inventory_items (store+variant metadata) + inventory_levels (per location quantities)
-- 1. Rename current inventory_items to legacy and drop old indexes (they keep original names after rename)
ALTER TABLE inventory_items RENAME TO inventory_items_legacy;
DROP INDEX IF EXISTS idx_inventory_items_store_id;
DROP INDEX IF EXISTS idx_inventory_items_variant_id;
DROP INDEX IF EXISTS idx_inventory_items_store_variant;
DROP INDEX IF EXISTS idx_inventory_items_variant_location;

-- 2. Create new inventory_items (one row per store + variant)
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    tracked BOOLEAN DEFAULT TRUE,
    requires_shipping BOOLEAN DEFAULT TRUE,
    cost_amount DECIMAL(12,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX idx_inventory_items_store_variant ON inventory_items(store_id, product_variant_id);
CREATE INDEX idx_inventory_items_store_id ON inventory_items(store_id);
CREATE INDEX idx_inventory_items_variant_id ON inventory_items(product_variant_id);

-- 3. Create inventory_levels (one row per inventory_item + location)
CREATE TABLE inventory_levels (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    inventory_item_id BIGINT NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    location_id BIGINT NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    quantity_incoming INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(inventory_item_id, location_id)
);
CREATE INDEX idx_inventory_levels_item ON inventory_levels(inventory_item_id);
CREATE INDEX idx_inventory_levels_location ON inventory_levels(location_id);

-- 4. Insert one new inventory_item per (store_id, product_variant_id) from legacy
INSERT INTO inventory_items (public_id, store_id, product_variant_id, tracked, requires_shipping, cost_amount, created_at, updated_at)
SELECT gen_random_uuid()::text, store_id, product_variant_id, true, true, NULL,
       MIN(created_at), MAX(updated_at)
FROM inventory_items_legacy
GROUP BY store_id, product_variant_id;

-- 5. Insert inventory_levels from legacy (join legacy to new items by store_id + product_variant_id)
INSERT INTO inventory_levels (public_id, inventory_item_id, location_id, quantity_available, quantity_reserved, quantity_incoming, created_at, updated_at)
SELECT gen_random_uuid()::text, n.id, l.location_id, l.quantity_available, l.quantity_reserved, 0, l.created_at, l.updated_at
FROM inventory_items_legacy l
JOIN inventory_items n ON n.store_id = l.store_id AND n.product_variant_id = l.product_variant_id;

-- 6. Drop FK from movements to legacy so we can repoint to new table
ALTER TABLE inventory_movements DROP CONSTRAINT inventory_movements_inventory_item_id_fkey;

-- 7. Update inventory_movements to point to new inventory_item id
UPDATE inventory_movements m
SET inventory_item_id = n.id
FROM inventory_items_legacy l, inventory_items n
WHERE m.inventory_item_id = l.id
  AND n.store_id = l.store_id AND n.product_variant_id = l.product_variant_id;

-- 8. Re-add FK to new inventory_items
ALTER TABLE inventory_movements ADD CONSTRAINT inventory_movements_inventory_item_id_fkey
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE;

-- 9. Drop legacy table
DROP TABLE inventory_items_legacy;
