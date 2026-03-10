-- Carrier abstraction for external logistics partners
ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS carrier_code VARCHAR(30) DEFAULT 'INTERNAL';
ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS external_shipment_id VARCHAR(255);
ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS tracking_url VARCHAR(500);
