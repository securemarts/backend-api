-- Store customers (merchant-managed, store-scoped)
CREATE TABLE store_customers (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    credit_limit DECIMAL(19, 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_store_customers_store ON store_customers(store_id);
CREATE UNIQUE INDEX idx_store_customers_store_phone ON store_customers(store_id, phone);

-- Invoices
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    store_customer_id BIGINT NOT NULL REFERENCES store_customers(id) ON DELETE RESTRICT,
    invoice_number VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    due_date DATE,
    issued_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_store ON invoices(store_id);
CREATE INDEX idx_invoices_store_customer ON invoices(store_customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE UNIQUE INDEX idx_invoices_store_number ON invoices(store_id, invoice_number);

-- Invoice line items
CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    description TEXT,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_price DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoice_items_invoice ON invoice_items(invoice_id);

-- Payments against invoices
CREATE TABLE invoice_payments (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    payment_method VARCHAR(30) NOT NULL,
    reference VARCHAR(255),
    paid_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoice_payments_invoice ON invoice_payments(invoice_id);

-- Link offline transactions to store customer and invoice (credit sales)
ALTER TABLE offline_transactions
    ADD COLUMN store_customer_id BIGINT REFERENCES store_customers(id) ON DELETE SET NULL,
    ADD COLUMN invoice_id BIGINT REFERENCES invoices(id) ON DELETE SET NULL;

CREATE INDEX idx_offline_transactions_store_customer ON offline_transactions(store_customer_id);
CREATE INDEX idx_offline_transactions_invoice ON offline_transactions(invoice_id);
