-- Optional delivery address on order (for creating delivery on payment success)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_address VARCHAR(500);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_lat DECIMAL(10,7);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_lng DECIMAL(10,7);
