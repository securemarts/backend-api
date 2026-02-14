-- Add latitude/longitude to store_profiles and locations for discovery and ETA
ALTER TABLE store_profiles
    ADD COLUMN latitude DECIMAL(10, 7),
    ADD COLUMN longitude DECIMAL(10, 7);

CREATE INDEX idx_store_profiles_state_city ON store_profiles(state, city);
CREATE INDEX idx_store_profiles_lat_lng ON store_profiles(latitude, longitude);

ALTER TABLE locations
    ADD COLUMN latitude DECIMAL(10, 7),
    ADD COLUMN longitude DECIMAL(10, 7),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN state VARCHAR(50);

CREATE INDEX idx_locations_store_city ON locations(store_id, city);
CREATE INDEX idx_locations_lat_lng ON locations(latitude, longitude);
