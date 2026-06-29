-- V19__create_account.sql
-- Chart of accounts with parent hierarchy for double-entry bookkeeping.
-- Self-referential FK: a child account references the same table via parent_account_id.

CREATE TABLE account (
    id                 CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id             CHAR(36)            NOT NULL,
    parent_account_id  CHAR(36),
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
    created_at         DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_account_number_org UNIQUE (org_id, account_number),
    CONSTRAINT fk_account_org    FOREIGN KEY (org_id)            REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_account_parent FOREIGN KEY (parent_account_id) REFERENCES account (id)      ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_account_org    ON account (org_id);
CREATE INDEX idx_account_parent ON account (parent_account_id);
CREATE INDEX idx_account_type   ON account (account_type);
