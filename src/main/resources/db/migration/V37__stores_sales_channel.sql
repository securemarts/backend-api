-- Sales channel: ONLINE (e-commerce only), RETAIL (physical only), BOTH
ALTER TABLE stores ADD COLUMN sales_channel VARCHAR(20) NOT NULL DEFAULT 'BOTH';
CREATE INDEX idx_stores_sales_channel ON stores(sales_channel);
