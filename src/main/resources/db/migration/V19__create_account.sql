-- V19__create_account.sql
-- Chart of accounts with parent hierarchy for double-entry bookkeeping

CREATE TABLE account (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id             UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    parent_account_id  UUID                REFERENCES account (id) ON DELETE RESTRICT,
    account_number     VARCHAR(20)         NOT NULL,
    account_name       VARCHAR(150)        NOT NULL,
    account_type       VARCHAR(50)         NOT NULL
                           CHECK (account_type IN ('asset', 'liability', 'equity', 'revenue', 'expense')),
    account_subtype    VARCHAR(100),
    normal_balance     VARCHAR(6)          NOT NULL DEFAULT 'debit'
                           CHECK (normal_balance IN ('debit', 'credit')),
    is_active          BOOLEAN             NOT NULL DEFAULT TRUE,
    is_system          BOOLEAN             NOT NULL DEFAULT FALSE,
    description        TEXT,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_account_number_org UNIQUE (org_id, account_number)
);

CREATE INDEX idx_account_org    ON account (org_id);
CREATE INDEX idx_account_parent ON account (parent_account_id);
CREATE INDEX idx_account_type   ON account (account_type);
