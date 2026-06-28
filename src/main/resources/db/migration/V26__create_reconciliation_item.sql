-- V26__create_reconciliation_item.sql
-- Line items within a reconciliation session marking each transaction as cleared or outstanding

CREATE TABLE reconciliation_item (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_id    UUID                NOT NULL REFERENCES bank_reconciliation (id) ON DELETE CASCADE,
    bank_transaction_id  UUID                NOT NULL REFERENCES bank_transaction (id) ON DELETE RESTRICT,
    is_cleared           BOOLEAN             NOT NULL DEFAULT FALSE,
    item_type            VARCHAR(50)         NOT NULL DEFAULT 'transaction'
                             CHECK (item_type IN ('transaction', 'deposit_in_transit', 'outstanding_check', 'adjustment')),
    notes                TEXT,

    CONSTRAINT uq_reconciliation_item UNIQUE (reconciliation_id, bank_transaction_id)
);

CREATE INDEX idx_reconciliation_item_reconciliation ON reconciliation_item (reconciliation_id);
CREATE INDEX idx_reconciliation_item_transaction    ON reconciliation_item (bank_transaction_id);
