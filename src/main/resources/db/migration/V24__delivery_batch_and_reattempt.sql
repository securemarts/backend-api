-- Optional batch for grouping deliveries per rider trip; supports reattempt (reschedule)
ALTER TABLE delivery_orders ADD COLUMN batch_id VARCHAR(36);
CREATE INDEX idx_delivery_orders_batch ON delivery_orders(batch_id);
