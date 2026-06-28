-- V20__create_journal_entry.sql
-- Header record for every double-entry transaction; total_debit must equal total_credit

CREATE TABLE journal_entry (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    created_by     UUID                REFERENCES person (id) ON DELETE SET NULL,
    approved_by    UUID                REFERENCES person (id) ON DELETE SET NULL,
    entry_number   VARCHAR(50)         UNIQUE,
    entry_date     DATE                NOT NULL,
    description    TEXT                NOT NULL,
    reference      VARCHAR(150),
    entry_type     VARCHAR(50)         NOT NULL DEFAULT 'general'
                       CHECK (entry_type IN ('general', 'adjusting', 'closing', 'reversing', 'opening')),
    status         VARCHAR(30)         NOT NULL DEFAULT 'draft'
                       CHECK (status IN ('draft', 'posted', 'void')),
    total_debit    NUMERIC(15, 2)      NOT NULL DEFAULT 0.00,
    total_credit   NUMERIC(15, 2)      NOT NULL DEFAULT 0.00,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    approved_at    TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_journal_balanced CHECK (total_debit = total_credit)
);

CREATE INDEX idx_journal_entry_org    ON journal_entry (org_id);
CREATE INDEX idx_journal_entry_date   ON journal_entry (entry_date);
CREATE INDEX idx_journal_entry_status ON journal_entry (status);
