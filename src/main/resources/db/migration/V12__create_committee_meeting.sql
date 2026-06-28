-- V12__create_committee_meeting.sql
-- Meeting records with agenda and minutes document links

CREATE TABLE committee_meeting (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    committee_id      UUID                NOT NULL REFERENCES committee (id) ON DELETE CASCADE,
    meeting_datetime  TIMESTAMP WITH TIME ZONE NOT NULL,
    location          VARCHAR(255),
    meeting_type      VARCHAR(50)         NOT NULL DEFAULT 'regular'
                          CHECK (meeting_type IN ('regular', 'special', 'emergency', 'annual')),
    agenda_url        VARCHAR(500),
    minutes_url       VARCHAR(500),
    status            VARCHAR(30)         NOT NULL DEFAULT 'scheduled'
                          CHECK (status IN ('scheduled', 'completed', 'cancelled', 'postponed')),
    notes             TEXT
);

CREATE INDEX idx_committee_meeting_committee ON committee_meeting (committee_id);
CREATE INDEX idx_committee_meeting_datetime  ON committee_meeting (meeting_datetime);
