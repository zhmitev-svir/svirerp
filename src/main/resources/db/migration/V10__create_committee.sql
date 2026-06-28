-- V10__create_committee.sql
-- Standing and ad hoc committees with lifecycle tracking

CREATE TABLE committee (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    name              VARCHAR(150)        NOT NULL,
    description       TEXT,
    committee_type    VARCHAR(100),
    is_standing       BOOLEAN             NOT NULL DEFAULT TRUE,
    is_active         BOOLEAN             NOT NULL DEFAULT TRUE,
    formed_date       DATE,
    dissolved_date    DATE,
    meeting_schedule  VARCHAR(255),
    mandate           TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_committee_name_org UNIQUE (org_id, name)
);

CREATE INDEX idx_committee_org    ON committee (org_id);
CREATE INDEX idx_committee_active ON committee (is_active);
