-- V18__create_fund.sql
-- Fund accounting: restricted vs unrestricted funds for nonprofit compliance.

CREATE TABLE fund (
    id                    CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id                CHAR(36)            NOT NULL,
    fund_name             VARCHAR(150)        NOT NULL,
    fund_code             VARCHAR(30)         NOT NULL,
    fund_type             VARCHAR(50)         NOT NULL DEFAULT 'unrestricted'
                              CHECK (fund_type IN ('unrestricted', 'temporarily_restricted', 'permanently_restricted')),
    description           TEXT,
    is_restricted         BOOLEAN             NOT NULL DEFAULT FALSE,
    restriction_purpose   TEXT,
    is_active             BOOLEAN             NOT NULL DEFAULT TRUE,
    opening_balance       DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,

    CONSTRAINT uq_fund_code_org UNIQUE (org_id, fund_code),
    CONSTRAINT fk_fund_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_fund_org ON fund (org_id);
