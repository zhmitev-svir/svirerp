-- V29__create_meeting_minutes.sql
-- General board/trustee meeting records (org-level, not tied to a committee).

CREATE TABLE meeting_minutes (
    id            CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id        CHAR(36)            NOT NULL,
    meeting_date  DATE                NOT NULL,
    title         VARCHAR(255)        NOT NULL,
    summary       TEXT,
    created_at    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_meeting_minutes_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_meeting_minutes_org  ON meeting_minutes (org_id);
CREATE INDEX idx_meeting_minutes_date ON meeting_minutes (meeting_date);
