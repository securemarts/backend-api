-- Allow reserve/release to track which level was used so we can add back to the correct level
ALTER TABLE inventory_movements ADD COLUMN inventory_level_id BIGINT REFERENCES inventory_levels(id) ON DELETE SET NULL;
CREATE INDEX idx_inventory_movements_level_id ON inventory_movements(inventory_level_id);
