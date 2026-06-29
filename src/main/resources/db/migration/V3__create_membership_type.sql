-- V3__create_membership_type.sql
-- Defines tiers such as General, Sustaining, Honorary with associated fees and durations.
-- MySQL: foreign keys must be declared as table-level constraints, not inline REFERENCES.

CREATE TABLE membership_type (
    id               CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id           CHAR(36)            NOT NULL,
    name             VARCHAR(100)        NOT NULL,
    description      TEXT,
    annual_fee       DECIMAL(10, 2)      NOT NULL DEFAULT 0.00,
    duration_months  INT                 NOT NULL DEFAULT 12,
    is_active        BOOLEAN             NOT NULL DEFAULT TRUE,
    benefits         TEXT,
    max_members      INT,
    created_at       DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_membership_type_name_org UNIQUE (org_id, name),
    CONSTRAINT fk_membership_type_org FOREIGN KEY (org_id)
        REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_membership_type_org ON membership_type (org_id);
