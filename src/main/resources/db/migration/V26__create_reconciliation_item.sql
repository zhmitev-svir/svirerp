-- V26__create_reconciliation_item.sql
-- Line items within a reconciliation session marking each transaction as cleared or outstanding.

CREATE TABLE reconciliation_item (
    id                   CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    reconciliation_id    CHAR(36)            NOT NULL,
    bank_transaction_id  CHAR(36)            NOT NULL,
    is_cleared           BOOLEAN             NOT NULL DEFAULT FALSE,
    item_type            VARCHAR(50)         NOT NULL DEFAULT 'transaction'
                             CHECK (item_type IN ('transaction', 'deposit_in_transit', 'outstanding_check', 'adjustment')),
    notes                TEXT,

    CONSTRAINT uq_reconciliation_item UNIQUE (reconciliation_id, bank_transaction_id),
    CONSTRAINT fk_reconciliation_item_reconciliation   FOREIGN KEY (reconciliation_id)   REFERENCES bank_reconciliation (id) ON DELETE CASCADE,
    CONSTRAINT fk_reconciliation_item_bank_transaction FOREIGN KEY (bank_transaction_id) REFERENCES bank_transaction (id)   ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_reconciliation_item_reconciliation ON reconciliation_item (reconciliation_id);
CREATE INDEX idx_reconciliation_item_transaction    ON reconciliation_item (bank_transaction_id);
