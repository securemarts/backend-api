ALTER TABLE stores ADD COLUMN business_type_id BIGINT REFERENCES business_types(id);

CREATE INDEX idx_stores_business_type_id ON stores(business_type_id);
