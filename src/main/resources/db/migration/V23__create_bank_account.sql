-- V23__create_bank_account.sql
-- Organization bank accounts linked to GL accounts for reconciliation

CREATE TABLE bank_account (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                  UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    gl_account_id           UUID                NOT NULL REFERENCES account (id) ON DELETE RESTRICT,
    institution_name        VARCHAR(150)        NOT NULL,
    account_name            VARCHAR(150)        NOT NULL,
    account_number_masked   VARCHAR(20),
    routing_number_masked   VARCHAR(20),
    account_type            VARCHAR(50)         NOT NULL DEFAULT 'checking'
                                CHECK (account_type IN ('checking', 'savings', 'money_market', 'cd', 'investment')),
    currency                VARCHAR(3)          NOT NULL DEFAULT 'USD',
    current_balance         NUMERIC(15, 2)      NOT NULL DEFAULT 0.00,
    opened_date             DATE,
    is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
    is_primary              BOOLEAN             NOT NULL DEFAULT FALSE,
    contact_name            VARCHAR(150),
    contact_phone           VARCHAR(30),
    notes                   TEXT,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_account_org ON bank_account (org_id);
