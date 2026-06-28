-- V3__create_membership_type.sql
-- Defines tiers such as General, Sustaining, Honorary with associated fees and durations

CREATE TABLE membership_type (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id           UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    name             VARCHAR(100)        NOT NULL,
    description      TEXT,
    annual_fee       NUMERIC(10, 2)      NOT NULL DEFAULT 0.00,
    duration_months  INTEGER             NOT NULL DEFAULT 12,
    is_active        BOOLEAN             NOT NULL DEFAULT TRUE,
    benefits         TEXT,
    max_members      INTEGER,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_membership_type_name_org UNIQUE (org_id, name)
);

CREATE INDEX idx_membership_type_org ON membership_type (org_id);
