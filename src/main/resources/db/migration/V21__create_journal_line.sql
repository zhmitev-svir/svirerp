-- V21__create_journal_line.sql
-- Individual debit/credit lines belonging to a journal entry; each line hits one account in one fund

CREATE TABLE journal_line (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id  UUID                NOT NULL REFERENCES journal_entry (id) ON DELETE CASCADE,
    account_id        UUID                NOT NULL REFERENCES account (id) ON DELETE RESTRICT,
    fund_id           UUID                REFERENCES fund (id) ON DELETE RESTRICT,
    description       TEXT,
    debit_amount      NUMERIC(15, 2)      NOT NULL DEFAULT 0.00 CHECK (debit_amount >= 0),
    credit_amount     NUMERIC(15, 2)      NOT NULL DEFAULT 0.00 CHECK (credit_amount >= 0),
    memo              VARCHAR(255),

    CONSTRAINT chk_journal_line_one_side CHECK (
        (debit_amount > 0 AND credit_amount = 0) OR
        (credit_amount > 0 AND debit_amount = 0)
    )
);

CREATE INDEX idx_journal_line_entry   ON journal_line (journal_entry_id);
CREATE INDEX idx_journal_line_account ON journal_line (account_id);
CREATE INDEX idx_journal_line_fund    ON journal_line (fund_id);
