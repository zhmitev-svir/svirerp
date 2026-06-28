-- V8__create_volunteer.sql
-- Volunteer roster linked to a person record

CREATE TABLE volunteer (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id       UUID                NOT NULL REFERENCES person (id) ON DELETE RESTRICT,
    org_id          UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    onboard_date    DATE                NOT NULL,
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    skills          TEXT,
    availability    VARCHAR(255),
    notes           TEXT
);

CREATE INDEX idx_volunteer_person ON volunteer (person_id);
CREATE INDEX idx_volunteer_org    ON volunteer (org_id);
CREATE INDEX idx_volunteer_active ON volunteer (is_active);
