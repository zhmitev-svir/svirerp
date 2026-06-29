-- V24__create_bank_transaction.sql
-- Raw bank transactions imported from statements or entered manually.

CREATE TABLE bank_transaction (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    bank_account_id   CHAR(36)            NOT NULL,
    journal_entry_id  CHAR(36),
    transaction_ref   VARCHAR(100),
    transaction_date  DATE                NOT NULL,
    posted_date       DATE,
    amount            DECIMAL(15, 2)      NOT NULL,
    transaction_type  VARCHAR(50)         NOT NULL
                          CHECK (transaction_type IN ('debit', 'credit', 'check', 'transfer', 'fee', 'interest', 'other')),
    description       TEXT,
    payee             VARCHAR(255),
    check_number      VARCHAR(20),
    status            VARCHAR(30)         NOT NULL DEFAULT 'pending'
                          CHECK (status IN ('pending', 'cleared', 'void')),
    is_reconciled     BOOLEAN             NOT NULL DEFAULT FALSE,
    imported_at       DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bank_transaction_account       FOREIGN KEY (bank_account_id)  REFERENCES bank_account (id)  ON DELETE CASCADE,
    CONSTRAINT fk_bank_transaction_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entry (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_bank_transaction_account    ON bank_transaction (bank_account_id);
CREATE INDEX idx_bank_transaction_date       ON bank_transaction (transaction_date);
CREATE INDEX idx_bank_transaction_reconciled ON bank_transaction (is_reconciled);
