-- Product catalog (multi-tenant by store_id)
CREATE TABLE collections (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    handle VARCHAR(100),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_collections_store_id ON collections(store_id);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tags_store_id ON tags(store_id);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    collection_id BIGINT REFERENCES collections(id) ON DELETE SET NULL,
    title VARCHAR(500) NOT NULL,
    handle VARCHAR(255),
    body_html TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    seo_title VARCHAR(70),
    seo_description VARCHAR(320),
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_store_id ON products(store_id);
CREATE INDEX idx_products_collection_id ON products(collection_id);
CREATE INDEX idx_products_status ON products(status);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100),
    title VARCHAR(255),
    price_amount DECIMAL(19,4) NOT NULL,
    compare_at_amount DECIMAL(19,4),
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    attributes_json TEXT,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_product_variants_sku_store ON product_variants(sku, product_id) WHERE sku IS NOT NULL;
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);

CREATE TABLE product_media (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    alt TEXT,
    position INT NOT NULL DEFAULT 0,
    media_type VARCHAR(20) DEFAULT 'image',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_media_product_id ON product_media(product_id);

CREATE TABLE product_tags (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, tag_id)
);

CREATE TABLE metafields (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    owner_type VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL,
    namespace VARCHAR(100) NOT NULL,
    key_name VARCHAR(100) NOT NULL,
    value_text TEXT,
    value_type VARCHAR(20) DEFAULT 'string',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_metafields_owner_ns_key ON metafields(owner_type, owner_id, namespace, key_name);
