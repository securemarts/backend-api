-- Products belong to store (not business). Stores have locations for inventory.

-- products: add store_id, backfill from first store per business, drop business_id
ALTER TABLE products ADD COLUMN store_id BIGINT REFERENCES stores(id) ON DELETE CASCADE;

UPDATE products p
SET store_id = (SELECT s.id FROM stores s WHERE s.business_id = p.business_id ORDER BY s.id LIMIT 1)
WHERE p.business_id IS NOT NULL;

ALTER TABLE products ALTER COLUMN store_id SET NOT NULL;
CREATE INDEX idx_products_store_id ON products(store_id);

DROP INDEX IF EXISTS idx_products_business_id;
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_business_id_fkey;
ALTER TABLE products DROP COLUMN business_id;
