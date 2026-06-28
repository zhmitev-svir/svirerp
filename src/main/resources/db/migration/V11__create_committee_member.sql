-- V11__create_committee_member.sql
-- Associates persons with committees, preserving historical membership via date ranges

CREATE TABLE committee_member (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    committee_id    UUID                NOT NULL REFERENCES committee (id) ON DELETE CASCADE,
    person_id       UUID                NOT NULL REFERENCES person (id) ON DELETE RESTRICT,
    role            VARCHAR(100),
    start_date      DATE                NOT NULL,
    end_date        DATE,
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    is_chair        BOOLEAN             NOT NULL DEFAULT FALSE,
    notes           TEXT
);

CREATE INDEX idx_committee_member_committee ON committee_member (committee_id);
CREATE INDEX idx_committee_member_person    ON committee_member (person_id);
