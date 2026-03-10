-- Allow multiple delivery orders per order (one per shipment)
DROP INDEX IF EXISTS idx_delivery_orders_order_id;
ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS shipment_id BIGINT;
CREATE INDEX idx_delivery_orders_order_id ON delivery_orders(order_id);
CREATE UNIQUE INDEX idx_delivery_orders_shipment_id ON delivery_orders(shipment_id) WHERE shipment_id IS NOT NULL;
