-- V36__create_vendor.sql
-- Vendors/payees that expense transactions link to (insurance co, utility co, contractors, suppliers).

CREATE TABLE vendor (
    id             CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id         CHAR(36)            NOT NULL,
    name           VARCHAR(150)        NOT NULL,
    category       VARCHAR(100),
    contact_name   VARCHAR(150),
    phone          VARCHAR(30),
    email          VARCHAR(150),
    address_line1  VARCHAR(255),
    city           VARCHAR(100),
    state          VARCHAR(50),
    zip            VARCHAR(20),
    notes          TEXT,
    is_active      BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vendor_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_vendor_org ON vendor (org_id);
