-- V25__create_bank_reconciliation.sql
-- Formal monthly reconciliation sessions comparing statement balance to book balance

CREATE TABLE bank_reconciliation (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_account_id    UUID                NOT NULL REFERENCES bank_account (id) ON DELETE CASCADE,
    reconciled_by      UUID                REFERENCES person (id) ON DELETE SET NULL,
    statement_date     DATE                NOT NULL,
    statement_balance  NUMERIC(15, 2)      NOT NULL,
    book_balance       NUMERIC(15, 2)      NOT NULL,
    difference         NUMERIC(15, 2)      GENERATED ALWAYS AS (statement_balance - book_balance) STORED,
    status             VARCHAR(30)         NOT NULL DEFAULT 'in_progress'
                           CHECK (status IN ('in_progress', 'completed', 'discrepancy')),
    reconciled_at      TIMESTAMP WITH TIME ZONE,
    notes              TEXT,

    CONSTRAINT uq_reconciliation_account_date UNIQUE (bank_account_id, statement_date)
);

CREATE INDEX idx_bank_reconciliation_account ON bank_reconciliation (bank_account_id);
CREATE INDEX idx_bank_reconciliation_date    ON bank_reconciliation (statement_date);
