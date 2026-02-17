-- Customer favorites (wishlist): user + store + product
CREATE TABLE customer_favorites (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, store_id, product_id)
);

CREATE INDEX idx_customer_favorites_user_id ON customer_favorites(user_id);
CREATE INDEX idx_customer_favorites_store_id ON customer_favorites(store_id);
CREATE INDEX idx_customer_favorites_product_id ON customer_favorites(product_id);
