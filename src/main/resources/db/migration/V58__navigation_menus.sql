CREATE TABLE menus (
    id          BIGSERIAL    PRIMARY KEY,
    public_id   VARCHAR(36)  NOT NULL UNIQUE,
    store_id    BIGINT       NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    handle      VARCHAR(100) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, handle)
);

CREATE INDEX idx_menus_store_id ON menus(store_id);

CREATE TABLE menu_items (
    id          BIGSERIAL    PRIMARY KEY,
    public_id   VARCHAR(36)  NOT NULL UNIQUE,
    menu_id     BIGINT       NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    parent_id   BIGINT       REFERENCES menu_items(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    resource_id VARCHAR(36),
    url         VARCHAR(1000),
    position    INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_menu_items_menu_id ON menu_items(menu_id);
CREATE INDEX idx_menu_items_parent_id ON menu_items(parent_id);
