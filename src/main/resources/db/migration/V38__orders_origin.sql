-- Order origin/channel for reporting: ONLINE (e-commerce), PICKUP (future in-store pickup)
ALTER TABLE orders ADD COLUMN origin VARCHAR(20) NOT NULL DEFAULT 'ONLINE';
CREATE INDEX idx_orders_origin ON orders(origin);
