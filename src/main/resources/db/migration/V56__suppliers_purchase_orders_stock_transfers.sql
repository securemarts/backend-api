-- Suppliers
CREATE TABLE suppliers (
    id              BIGSERIAL PRIMARY KEY,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    store_id        BIGINT       NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    company         VARCHAR(255),
    address1        VARCHAR(500),
    address2        VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(50),
    country         VARCHAR(100),
    postal_code     VARCHAR(20),
    notes           TEXT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_suppliers_store_id ON suppliers(store_id);
CREATE INDEX idx_suppliers_store_active ON suppliers(store_id, active);

-- Purchase Orders
CREATE TABLE purchase_orders (
    id                  BIGSERIAL PRIMARY KEY,
    public_id           VARCHAR(36)  NOT NULL UNIQUE,
    store_id            BIGINT       NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    supplier_id         BIGINT       REFERENCES suppliers(id) ON DELETE SET NULL,
    destination_id      BIGINT       REFERENCES locations(id) ON DELETE SET NULL,
    number              VARCHAR(50)  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    currency            VARCHAR(3)   NOT NULL DEFAULT 'NGN',
    shipping_cost       DECIMAL(12, 2) NOT NULL DEFAULT 0,
    adjustments_cost    DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_cost            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    note                TEXT,
    expected_on         DATE,
    ordered_at          TIMESTAMPTZ,
    received_at         TIMESTAMPTZ,
    payment_due_on      DATE,
    paid                BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_purchase_orders_store_id ON purchase_orders(store_id);
CREATE INDEX idx_purchase_orders_store_status ON purchase_orders(store_id, status);
CREATE UNIQUE INDEX idx_purchase_orders_store_number ON purchase_orders(store_id, number);
CREATE INDEX idx_purchase_orders_supplier_id ON purchase_orders(supplier_id);

-- Purchase Order Line Items
CREATE TABLE purchase_order_line_items (
    id                  BIGSERIAL PRIMARY KEY,
    public_id           VARCHAR(36) NOT NULL UNIQUE,
    purchase_order_id   BIGINT      NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    inventory_item_id   BIGINT      NOT NULL REFERENCES inventory_items(id) ON DELETE RESTRICT,
    product_variant_id  BIGINT      NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    quantity            INT         NOT NULL,
    received_quantity   INT         NOT NULL DEFAULT 0,
    rejected_quantity   INT         NOT NULL DEFAULT 0,
    cost_price          DECIMAL(12, 2),
    retail_price        DECIMAL(12, 2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_po_line_items_po_id ON purchase_order_line_items(purchase_order_id);
CREATE INDEX idx_po_line_items_inventory_item ON purchase_order_line_items(inventory_item_id);

-- Stock Transfers
CREATE TABLE stock_transfers (
    id                      BIGSERIAL PRIMARY KEY,
    public_id               VARCHAR(36)  NOT NULL UNIQUE,
    store_id                BIGINT       NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    origin_location_id      BIGINT       REFERENCES locations(id) ON DELETE SET NULL,
    destination_location_id BIGINT       REFERENCES locations(id) ON DELETE SET NULL,
    number                  VARCHAR(50)  NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    expected_arrival_date   DATE,
    shipped_at              TIMESTAMPTZ,
    received_at             TIMESTAMPTZ,
    note                    TEXT,
    reference_name          VARCHAR(255),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_transfers_store_id ON stock_transfers(store_id);
CREATE INDEX idx_stock_transfers_store_status ON stock_transfers(store_id, status);
CREATE UNIQUE INDEX idx_stock_transfers_store_number ON stock_transfers(store_id, number);

-- Stock Transfer Line Items
CREATE TABLE stock_transfer_line_items (
    id                  BIGSERIAL PRIMARY KEY,
    public_id           VARCHAR(36) NOT NULL UNIQUE,
    stock_transfer_id   BIGINT      NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,
    inventory_item_id   BIGINT      NOT NULL REFERENCES inventory_items(id) ON DELETE RESTRICT,
    product_variant_id  BIGINT      NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    quantity            INT         NOT NULL,
    received_quantity   INT         NOT NULL DEFAULT 0,
    rejected_quantity   INT         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_st_line_items_transfer_id ON stock_transfer_line_items(stock_transfer_id);
CREATE INDEX idx_st_line_items_inventory_item ON stock_transfer_line_items(inventory_item_id);
