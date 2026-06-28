-- V6__create_trustee.sql
-- Board trustees with term tracking and officer designation

CREATE TABLE trustee (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id    UUID                NOT NULL REFERENCES person (id) ON DELETE RESTRICT,
    org_id       UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    title        VARCHAR(100),
    role         VARCHAR(100)        NOT NULL,
    term_start   DATE                NOT NULL,
    term_end     DATE,
    is_active    BOOLEAN             NOT NULL DEFAULT TRUE,
    is_officer   BOOLEAN             NOT NULL DEFAULT FALSE,
    notes        TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trustee_person ON trustee (person_id);
CREATE INDEX idx_trustee_org    ON trustee (org_id);
CREATE INDEX idx_trustee_active ON trustee (is_active);
