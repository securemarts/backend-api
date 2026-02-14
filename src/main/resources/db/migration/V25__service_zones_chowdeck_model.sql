-- Chowdeck-style model: radius-based service zones, rider location/availability, store in zone, delivery fee by distance

-- Service zones (circle: center + radius_km; pricing: base_fee + per_km_fee)
CREATE TABLE service_zones (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    center_lat DECIMAL(10, 7) NOT NULL,
    center_lng DECIMAL(10, 7) NOT NULL,
    radius_km DECIMAL(8, 2) NOT NULL,
    base_fee DECIMAL(19, 4) NOT NULL,
    per_km_fee DECIMAL(19, 4) NOT NULL,
    max_distance_km DECIMAL(8, 2),
    min_order_amount DECIMAL(19, 4),
    surge_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_zones_city ON service_zones(city);
CREATE INDEX idx_service_zones_active ON service_zones(active);

-- Riders: belong to a zone; track current location; available for dispatch
ALTER TABLE riders
    ADD COLUMN zone_id BIGINT REFERENCES service_zones(id) ON DELETE SET NULL,
    ADD COLUMN current_lat DECIMAL(10, 7),
    ADD COLUMN current_lng DECIMAL(10, 7),
    ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_riders_zone_available ON riders(zone_id, is_available);

-- Store profile: which zone the store serves (store lat/lng already in store_profiles from V22)
ALTER TABLE store_profiles
    ADD COLUMN zone_id BIGINT REFERENCES service_zones(id) ON DELETE SET NULL;

CREATE INDEX idx_store_profiles_zone ON store_profiles(zone_id);

-- Delivery order: customer delivery location for zone check and fee calculation
ALTER TABLE delivery_orders
    ADD COLUMN delivery_lat DECIMAL(10, 7),
    ADD COLUMN delivery_lng DECIMAL(10, 7);

CREATE INDEX idx_delivery_orders_delivery_geo ON delivery_orders(delivery_lat, delivery_lng);
