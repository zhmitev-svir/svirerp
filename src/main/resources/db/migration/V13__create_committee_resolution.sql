-- V13__create_committee_resolution.sql
-- Formal resolutions produced at meetings with vote tallies.

CREATE TABLE committee_resolution (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    committee_id      CHAR(36)            NOT NULL,
    meeting_id        CHAR(36)            NOT NULL,
    resolution_number VARCHAR(50)         UNIQUE,
    title             VARCHAR(255)        NOT NULL,
    description       TEXT,
    status            VARCHAR(30)         NOT NULL DEFAULT 'passed'
                          CHECK (status IN ('passed', 'failed', 'tabled', 'withdrawn')),
    passed_date       DATE,
    votes_for         INT                 NOT NULL DEFAULT 0,
    votes_against     INT                 NOT NULL DEFAULT 0,
    abstentions       INT                 NOT NULL DEFAULT 0,

    CONSTRAINT fk_committee_resolution_committee FOREIGN KEY (committee_id) REFERENCES committee (id)         ON DELETE CASCADE,
    CONSTRAINT fk_committee_resolution_meeting   FOREIGN KEY (meeting_id)   REFERENCES committee_meeting (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_committee_resolution_committee ON committee_resolution (committee_id);
CREATE INDEX idx_committee_resolution_meeting   ON committee_resolution (meeting_id);
