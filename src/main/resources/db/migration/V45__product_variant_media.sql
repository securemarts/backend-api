CREATE TABLE product_variant_media (
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    media_id   BIGINT NOT NULL REFERENCES product_media(id) ON DELETE CASCADE,
    PRIMARY KEY (variant_id, media_id)
);

