-- V13__create_committee_resolution.sql
-- Formal resolutions produced at meetings with vote tallies

CREATE TABLE committee_resolution (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    committee_id      UUID                NOT NULL REFERENCES committee (id) ON DELETE CASCADE,
    meeting_id        UUID                NOT NULL REFERENCES committee_meeting (id) ON DELETE RESTRICT,
    resolution_number VARCHAR(50)         UNIQUE,
    title             VARCHAR(255)        NOT NULL,
    description       TEXT,
    status            VARCHAR(30)         NOT NULL DEFAULT 'passed'
                          CHECK (status IN ('passed', 'failed', 'tabled', 'withdrawn')),
    passed_date       DATE,
    votes_for         INTEGER             NOT NULL DEFAULT 0,
    votes_against     INTEGER             NOT NULL DEFAULT 0,
    abstentions       INTEGER             NOT NULL DEFAULT 0
);

CREATE INDEX idx_committee_resolution_committee ON committee_resolution (committee_id);
CREATE INDEX idx_committee_resolution_meeting   ON committee_resolution (meeting_id);
