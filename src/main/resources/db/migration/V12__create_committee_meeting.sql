-- V12__create_committee_meeting.sql
-- Meeting records with agenda and minutes document links.

CREATE TABLE committee_meeting (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    committee_id      CHAR(36)            NOT NULL,
    meeting_datetime  DATETIME            NOT NULL,
    location          VARCHAR(255),
    meeting_type      VARCHAR(50)         NOT NULL DEFAULT 'regular'
                          CHECK (meeting_type IN ('regular', 'special', 'emergency', 'annual')),
    agenda_url        VARCHAR(500),
    minutes_url       VARCHAR(500),
    status            VARCHAR(30)         NOT NULL DEFAULT 'scheduled'
                          CHECK (status IN ('scheduled', 'completed', 'cancelled', 'postponed')),
    notes             TEXT,

    CONSTRAINT fk_committee_meeting_committee FOREIGN KEY (committee_id) REFERENCES committee (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_committee_meeting_committee ON committee_meeting (committee_id);
CREATE INDEX idx_committee_meeting_datetime  ON committee_meeting (meeting_datetime);
