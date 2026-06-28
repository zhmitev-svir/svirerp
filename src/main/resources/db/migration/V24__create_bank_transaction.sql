-- V24__create_bank_transaction.sql
-- Raw bank transactions imported from statements or entered manually

CREATE TABLE bank_transaction (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_account_id   UUID                NOT NULL REFERENCES bank_account (id) ON DELETE CASCADE,
    journal_entry_id  UUID                REFERENCES journal_entry (id) ON DELETE SET NULL,
    transaction_ref   VARCHAR(100),
    transaction_date  DATE                NOT NULL,
    posted_date       DATE,
    amount            NUMERIC(15, 2)      NOT NULL,
    transaction_type  VARCHAR(50)         NOT NULL
                          CHECK (transaction_type IN ('debit', 'credit', 'check', 'transfer', 'fee', 'interest', 'other')),
    description       TEXT,
    payee             VARCHAR(255),
    check_number      VARCHAR(20),
    status            VARCHAR(30)         NOT NULL DEFAULT 'pending'
                          CHECK (status IN ('pending', 'cleared', 'void')),
    is_reconciled     BOOLEAN             NOT NULL DEFAULT FALSE,
    imported_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_transaction_account ON bank_transaction (bank_account_id);
CREATE INDEX idx_bank_transaction_date    ON bank_transaction (transaction_date);
CREATE INDEX idx_bank_transaction_reconciled ON bank_transaction (is_reconciled);
