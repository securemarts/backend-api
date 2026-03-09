-- Business types reference table
CREATE TABLE business_types (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_key VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed a richer set of common merchant categories so you rarely have to insert more by hand.
INSERT INTO business_types (public_id, code, name, description) VALUES
    (gen_random_uuid()::text, 'RESTAURANT', 'Restaurant / Food', 'Restaurants, bukas, food vendors, cloud kitchens'),
    (gen_random_uuid()::text, 'GROCERY', 'Grocery / Supermarket', 'Supermarkets, grocery and convenience stores'),
    (gen_random_uuid()::text, 'PHARMACY', 'Pharmacy', 'Drug stores and pharmacies'),
    (gen_random_uuid()::text, 'FASHION', 'Fashion / Boutique', 'Clothing, footwear and fashion accessories'),
    (gen_random_uuid()::text, 'ELECTRONICS', 'Electronics', 'Phones, computers and electronics retailers'),
    (gen_random_uuid()::text, 'BEAUTY', 'Beauty / Cosmetics', 'Makeup, skincare and cosmetics'),
    (gen_random_uuid()::text, 'HAIR_SALON', 'Salon / Barbershop', 'Hair salons and barbershops'),
    (gen_random_uuid()::text, 'BAKERY', 'Bakery', 'Bakeries, pastry and cake shops'),
    (gen_random_uuid()::text, 'CAFE', 'Cafe / Coffee shop', 'Cafes and coffee shops'),
    (gen_random_uuid()::text, 'HOME_FURNITURE', 'Home & Furniture', 'Furniture, home decor and household items'),
    (gen_random_uuid()::text, 'APPLIANCES', 'Home Appliances', 'Electronics and home appliances'),
    (gen_random_uuid()::text, 'AUTO_PARTS', 'Auto Parts / Workshop', 'Auto parts, mechanics and car services'),
    (gen_random_uuid()::text, 'HARDWARE', 'Hardware / Building', 'Building materials and hardware stores'),
    (gen_random_uuid()::text, 'BOOKS_STATIONERY', 'Books & Stationery', 'Bookshops and stationery stores'),
    (gen_random_uuid()::text, 'BABY_KIDS', 'Baby & Kids', 'Baby products, toys and kids fashion'),
    (gen_random_uuid()::text, 'HEALTH_CLINIC', 'Clinic / Health', 'Clinics, diagnostics and healthcare services'),
    (gen_random_uuid()::text, 'SERVICES', 'Services', 'Professional and other service businesses'),
    (gen_random_uuid()::text, 'LOGISTICS', 'Logistics / Delivery', 'Courier, dispatch and logistics services'),
    (gen_random_uuid()::text, 'LAUNDRY', 'Laundry / Dry cleaning', 'Laundry and dry cleaning services'),
    (gen_random_uuid()::text, 'AGRICULTURE', 'Agriculture / Agro', 'Agro produce and farm input businesses'),
    (gen_random_uuid()::text, 'TECH', 'Tech / Startup', 'SaaS, tech products and startups'),
    (gen_random_uuid()::text, 'FINTECH', 'Fintech / Financial services', 'Wallets, payments and other fintech services'),
    (gen_random_uuid()::text, 'NGO', 'NGO / Non-profit', 'Foundations, NGOs and non‑profits');

-- Business logo and type
ALTER TABLE businesses
    ADD COLUMN logo_url VARCHAR(512),
    ADD COLUMN business_type_id BIGINT REFERENCES business_types(id);

CREATE INDEX idx_businesses_business_type ON businesses(business_type_id);

