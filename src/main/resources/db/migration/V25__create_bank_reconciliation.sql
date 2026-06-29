-- V25__create_bank_reconciliation.sql
-- Formal monthly reconciliation sessions comparing statement balance to book balance.
--
-- 'difference' is a generated column (MySQL 8.0.17+ STORED expression).
-- MySQL syntax is identical to the SQL standard: GENERATED ALWAYS AS (...) STORED.
-- The JPA entity maps this column with insertable=false, updatable=false so Hibernate
-- never includes it in INSERT/UPDATE statements.

CREATE TABLE bank_reconciliation (
    id                 CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    bank_account_id    CHAR(36)            NOT NULL,
    reconciled_by      CHAR(36),
    statement_date     DATE                NOT NULL,
    statement_balance  DECIMAL(15, 2)      NOT NULL,
    book_balance       DECIMAL(15, 2)      NOT NULL,
    difference         DECIMAL(15, 2)      GENERATED ALWAYS AS (statement_balance - book_balance) STORED,
    status             VARCHAR(30)         NOT NULL DEFAULT 'in_progress'
                           CHECK (status IN ('in_progress', 'completed', 'discrepancy')),
    reconciled_at      DATETIME,
    notes              TEXT,

    CONSTRAINT uq_reconciliation_account_date UNIQUE (bank_account_id, statement_date),
    CONSTRAINT fk_bank_reconciliation_account       FOREIGN KEY (bank_account_id) REFERENCES bank_account (id) ON DELETE CASCADE,
    CONSTRAINT fk_bank_reconciliation_reconciled_by FOREIGN KEY (reconciled_by)   REFERENCES person (id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_bank_reconciliation_account ON bank_reconciliation (bank_account_id);
CREATE INDEX idx_bank_reconciliation_date    ON bank_reconciliation (statement_date);
