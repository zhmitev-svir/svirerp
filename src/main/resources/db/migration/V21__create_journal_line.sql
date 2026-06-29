-- V21__create_journal_line.sql
-- Individual debit/credit lines belonging to a journal entry; each line hits one account in one fund.

CREATE TABLE journal_line (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    journal_entry_id  CHAR(36)            NOT NULL,
    account_id        CHAR(36)            NOT NULL,
    fund_id           CHAR(36),
    description       TEXT,
    debit_amount      DECIMAL(15, 2)      NOT NULL DEFAULT 0.00 CHECK (debit_amount >= 0),
    credit_amount     DECIMAL(15, 2)      NOT NULL DEFAULT 0.00 CHECK (credit_amount >= 0),
    memo              VARCHAR(255),

    -- Exactly one side must be non-zero; prevents mixed debit/credit lines.
    CONSTRAINT chk_journal_line_one_side CHECK (
        (debit_amount > 0 AND credit_amount = 0) OR
        (credit_amount > 0 AND debit_amount = 0)
    ),

    CONSTRAINT fk_journal_line_entry   FOREIGN KEY (journal_entry_id) REFERENCES journal_entry (id) ON DELETE CASCADE,
    CONSTRAINT fk_journal_line_account FOREIGN KEY (account_id)       REFERENCES account (id)       ON DELETE RESTRICT,
    CONSTRAINT fk_journal_line_fund    FOREIGN KEY (fund_id)          REFERENCES fund (id)          ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_journal_line_entry   ON journal_line (journal_entry_id);
CREATE INDEX idx_journal_line_account ON journal_line (account_id);
CREATE INDEX idx_journal_line_fund    ON journal_line (fund_id);
