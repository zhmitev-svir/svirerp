-- V23__create_bank_account.sql
-- Organisation bank accounts linked to GL accounts for reconciliation.

CREATE TABLE bank_account (
    id                      CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id                  CHAR(36)            NOT NULL,
    gl_account_id           CHAR(36)            NOT NULL,
    institution_name        VARCHAR(150)        NOT NULL,
    account_name            VARCHAR(150)        NOT NULL,
    account_number_masked   VARCHAR(20),
    routing_number_masked   VARCHAR(20),
    account_type            VARCHAR(50)         NOT NULL DEFAULT 'checking'
                                CHECK (account_type IN ('checking', 'savings', 'money_market', 'cd', 'investment')),
    currency                VARCHAR(3)          NOT NULL DEFAULT 'USD',
    current_balance         DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    opened_date             DATE,
    is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
    is_primary              BOOLEAN             NOT NULL DEFAULT FALSE,
    contact_name            VARCHAR(150),
    contact_phone           VARCHAR(30),
    notes                   TEXT,
    updated_at              DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bank_account_org        FOREIGN KEY (org_id)       REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_bank_account_gl_account FOREIGN KEY (gl_account_id) REFERENCES account (id)     ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_bank_account_org ON bank_account (org_id);
