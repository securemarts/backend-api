-- Reservation expiry for checkout (release job uses this)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS reservation_expires_at TIMESTAMP WITH TIME ZONE;
