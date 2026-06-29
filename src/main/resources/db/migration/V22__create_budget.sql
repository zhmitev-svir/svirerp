-- V22__create_budget.sql
-- Annual budget allocations per account and fund.

CREATE TABLE budget (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id            CHAR(36)            NOT NULL,
    account_id        CHAR(36)            NOT NULL,
    fund_id           CHAR(36),
    fiscal_year       INT                 NOT NULL,
    budgeted_amount   DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    period            VARCHAR(20)         NOT NULL DEFAULT 'annual'
                          CHECK (period IN ('annual', 'q1', 'q2', 'q3', 'q4', 'monthly')),
    notes             TEXT,
    is_approved       BOOLEAN             NOT NULL DEFAULT FALSE,
    approved_date     DATE,

    CONSTRAINT uq_budget_account_fund_year_period UNIQUE (org_id, account_id, fund_id, fiscal_year, period),
    CONSTRAINT fk_budget_org     FOREIGN KEY (org_id)     REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_account FOREIGN KEY (account_id) REFERENCES account (id)      ON DELETE RESTRICT,
    CONSTRAINT fk_budget_fund    FOREIGN KEY (fund_id)    REFERENCES fund (id)         ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_budget_org         ON budget (org_id);
CREATE INDEX idx_budget_account     ON budget (account_id);
CREATE INDEX idx_budget_fiscal_year ON budget (fiscal_year);
