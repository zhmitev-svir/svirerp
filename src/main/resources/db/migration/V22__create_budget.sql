-- V22__create_budget.sql
-- Annual budget allocations per account and fund

CREATE TABLE budget (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    account_id        UUID                NOT NULL REFERENCES account (id) ON DELETE RESTRICT,
    fund_id           UUID                REFERENCES fund (id) ON DELETE RESTRICT,
    fiscal_year       INTEGER             NOT NULL,
    budgeted_amount   NUMERIC(15, 2)      NOT NULL DEFAULT 0.00,
    period            VARCHAR(20)         NOT NULL DEFAULT 'annual'
                          CHECK (period IN ('annual', 'q1', 'q2', 'q3', 'q4', 'monthly')),
    notes             TEXT,
    is_approved       BOOLEAN             NOT NULL DEFAULT FALSE,
    approved_date     DATE,

    CONSTRAINT uq_budget_account_fund_year_period UNIQUE (org_id, account_id, fund_id, fiscal_year, period)
);

CREATE INDEX idx_budget_org         ON budget (org_id);
CREATE INDEX idx_budget_account     ON budget (account_id);
CREATE INDEX idx_budget_fiscal_year ON budget (fiscal_year);
