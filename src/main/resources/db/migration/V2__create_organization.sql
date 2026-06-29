-- V2__create_organization.sql
-- Root anchor for the entire ERP; all domain tables reference org_id.

CREATE TABLE organization (
    id                  CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    name                VARCHAR(255)        NOT NULL,
    legal_name          VARCHAR(255),
    tax_id_ein          VARCHAR(20)         UNIQUE,
    nonprofit_type      VARCHAR(50),
    mission_statement   TEXT,
    address_line1       VARCHAR(255),
    address_line2       VARCHAR(255),
    city                VARCHAR(100),
    state               VARCHAR(100),
    zip                 VARCHAR(20),
    country             VARCHAR(100)        NOT NULL DEFAULT 'US',
    phone               VARCHAR(30),
    email               VARCHAR(255),
    website             VARCHAR(255),
    founded_date        DATE,
    fiscal_year_start   DATE,
    logo_url            VARCHAR(500),
    created_at          DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
