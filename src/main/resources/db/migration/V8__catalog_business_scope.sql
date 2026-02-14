-- Catalog scoped to business: products, collections, tags belong to business; stores keep inventory.

-- products: add business_id, backfill from store, drop store_id
ALTER TABLE products ADD COLUMN business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE;
UPDATE products p SET business_id = s.business_id FROM stores s WHERE p.store_id = s.id;
ALTER TABLE products ALTER COLUMN business_id SET NOT NULL;
CREATE INDEX idx_products_business_id ON products(business_id);
ALTER TABLE products DROP CONSTRAINT products_store_id_fkey;
DROP INDEX idx_products_store_id;
ALTER TABLE products DROP COLUMN store_id;

-- collections: add business_id, backfill from store, drop store_id
ALTER TABLE collections ADD COLUMN business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE;
UPDATE collections c SET business_id = s.business_id FROM stores s WHERE c.store_id = s.id;
ALTER TABLE collections ALTER COLUMN business_id SET NOT NULL;
CREATE INDEX idx_collections_business_id ON collections(business_id);
ALTER TABLE collections DROP CONSTRAINT collections_store_id_fkey;
DROP INDEX idx_collections_store_id;
ALTER TABLE collections DROP COLUMN store_id;

-- tags: add business_id, backfill from store, drop store_id
ALTER TABLE tags ADD COLUMN business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE;
UPDATE tags t SET business_id = s.business_id FROM stores s WHERE t.store_id = s.id;
ALTER TABLE tags ALTER COLUMN business_id SET NOT NULL;
CREATE INDEX idx_tags_business_id ON tags(business_id);
ALTER TABLE tags DROP CONSTRAINT tags_store_id_fkey;
DROP INDEX idx_tags_store_id;
ALTER TABLE tags DROP COLUMN store_id;
