-- Logistics & Delivery Network: hubs, routes, zones, riders, delivery orders, tracking, proof of delivery, pricing rules

-- Hubs (hub-and-spoke)
CREATE TABLE logistics_hubs (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    state VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_logistics_hubs_state ON logistics_hubs(state);
CREATE INDEX idx_logistics_hubs_city ON logistics_hubs(city);
CREATE INDEX idx_logistics_hubs_state_city ON logistics_hubs(state, city);

-- Delivery routes (origin hub -> destination hub)
CREATE TABLE delivery_routes (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    origin_hub_id BIGINT NOT NULL REFERENCES logistics_hubs(id) ON DELETE RESTRICT,
    destination_hub_id BIGINT NOT NULL REFERENCES logistics_hubs(id) ON DELETE RESTRICT,
    estimated_hours DECIMAL(5, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_route_different_hubs CHECK (origin_hub_id != destination_hub_id)
);

CREATE INDEX idx_delivery_routes_origin ON delivery_routes(origin_hub_id);
CREATE INDEX idx_delivery_routes_destination ON delivery_routes(destination_hub_id);
CREATE UNIQUE INDEX idx_delivery_routes_origin_dest ON delivery_routes(origin_hub_id, destination_hub_id);

-- Delivery zones (for pricing/assignment)
CREATE TABLE delivery_zones (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    hub_id BIGINT REFERENCES logistics_hubs(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    state VARCHAR(50),
    city VARCHAR(100),
    radius_km DECIMAL(8, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_zones_hub ON delivery_zones(hub_id);
CREATE INDEX idx_delivery_zones_state_city ON delivery_zones(state, city);

-- Riders (fleet; separate identity from users)
CREATE TABLE riders (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OFF_DUTY',
    current_hub_id BIGINT REFERENCES logistics_hubs(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_riders_phone ON riders(phone);
CREATE INDEX idx_riders_email ON riders(email);
CREATE INDEX idx_riders_status ON riders(status);
CREATE INDEX idx_riders_current_hub ON riders(current_hub_id);

-- Rider vehicles
CREATE TABLE rider_vehicles (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    rider_id BIGINT NOT NULL REFERENCES riders(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    plate_number VARCHAR(30),
    capacity_weight_kg DECIMAL(10, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rider_vehicles_rider ON rider_vehicles(rider_id);

-- Delivery orders (one per order that needs delivery)
CREATE TABLE delivery_orders (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE RESTRICT,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE RESTRICT,
    origin_hub_id BIGINT REFERENCES logistics_hubs(id) ON DELETE SET NULL,
    destination_hub_id BIGINT REFERENCES logistics_hubs(id) ON DELETE SET NULL,
    pickup_address VARCHAR(500),
    delivery_address VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rider_id BIGINT REFERENCES riders(id) ON DELETE SET NULL,
    pricing_amount DECIMAL(19, 4),
    pricing_currency VARCHAR(3) DEFAULT 'NGN',
    scheduled_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    failed_reason VARCHAR(500),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_delivery_orders_order_id ON delivery_orders(order_id);
CREATE INDEX idx_delivery_orders_store ON delivery_orders(store_id);
CREATE INDEX idx_delivery_orders_rider ON delivery_orders(rider_id);
CREATE INDEX idx_delivery_orders_status ON delivery_orders(status);
CREATE INDEX idx_delivery_orders_scheduled ON delivery_orders(scheduled_at);

-- Delivery tracking events (status history)
CREATE TABLE delivery_tracking_events (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    delivery_order_id BIGINT NOT NULL REFERENCES delivery_orders(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_tracking_events_delivery ON delivery_tracking_events(delivery_order_id);
CREATE INDEX idx_delivery_tracking_events_created ON delivery_tracking_events(delivery_order_id, created_at);

-- Proof of delivery
CREATE TABLE proof_of_delivery (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    delivery_order_id BIGINT NOT NULL REFERENCES delivery_orders(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    file_url VARCHAR(500),
    payload TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_proof_of_delivery_delivery ON proof_of_delivery(delivery_order_id);

-- Delivery pricing rules (zone or route based)
CREATE TABLE delivery_pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    zone_id BIGINT REFERENCES delivery_zones(id) ON DELETE CASCADE,
    origin_hub_id BIGINT REFERENCES logistics_hubs(id) ON DELETE CASCADE,
    destination_hub_id BIGINT REFERENCES logistics_hubs(id) ON DELETE CASCADE,
    base_amount DECIMAL(19, 4) NOT NULL,
    per_kg_amount DECIMAL(19, 4),
    same_city_multiplier DECIMAL(5, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_pricing_rules_zone ON delivery_pricing_rules(zone_id);
CREATE INDEX idx_delivery_pricing_rules_route ON delivery_pricing_rules(origin_hub_id, destination_hub_id);
