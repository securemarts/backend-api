-- BaseEntity requires updated_at; rider_documents was missing it
ALTER TABLE rider_documents
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
