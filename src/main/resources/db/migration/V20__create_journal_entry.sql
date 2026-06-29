-- V20__create_journal_entry.sql
-- Header record for every double-entry transaction; total_debit must equal total_credit.
-- Two separate FKs reference person(id): created_by and approved_by.

CREATE TABLE journal_entry (
    id             CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id         CHAR(36)            NOT NULL,
    created_by     CHAR(36),
    approved_by    CHAR(36),
    entry_number   VARCHAR(50)         UNIQUE,
    entry_date     DATE                NOT NULL,
    description    TEXT                NOT NULL,
    reference      VARCHAR(150),
    entry_type     VARCHAR(50)         NOT NULL DEFAULT 'general'
                       CHECK (entry_type IN ('general', 'adjusting', 'closing', 'reversing', 'opening')),
    status         VARCHAR(30)         NOT NULL DEFAULT 'draft'
                       CHECK (status IN ('draft', 'posted', 'void')),
    total_debit    DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    total_credit   DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    created_at     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at    DATETIME,

    -- MySQL supports multi-column CHECK constraints; enforces double-entry balance rule.
    CONSTRAINT chk_journal_balanced CHECK (total_debit = total_credit),

    CONSTRAINT fk_journal_entry_org         FOREIGN KEY (org_id)      REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_journal_entry_created_by  FOREIGN KEY (created_by)  REFERENCES person (id)       ON DELETE SET NULL,
    CONSTRAINT fk_journal_entry_approved_by FOREIGN KEY (approved_by) REFERENCES person (id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_journal_entry_org    ON journal_entry (org_id);
CREATE INDEX idx_journal_entry_date   ON journal_entry (entry_date);
CREATE INDEX idx_journal_entry_status ON journal_entry (status);
