-- V14__create_calendar_event.sql
-- Base event table for all organisation events; church events extend this via V15.
-- The deferred FK from volunteer_hour.event_id is added at the bottom of this script
-- now that calendar_event exists.

CREATE TABLE calendar_event (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id            CHAR(36)            NOT NULL,
    created_by        CHAR(36),
    title             VARCHAR(255)        NOT NULL,
    description       TEXT,
    event_type        VARCHAR(100)        NOT NULL DEFAULT 'general',
    start_datetime    DATETIME            NOT NULL,
    end_datetime      DATETIME,
    is_all_day        BOOLEAN             NOT NULL DEFAULT FALSE,
    location          VARCHAR(255),
    virtual_link      VARCHAR(500),
    is_recurring      BOOLEAN             NOT NULL DEFAULT FALSE,
    recurrence_rule   VARCHAR(255),
    status            VARCHAR(30)         NOT NULL DEFAULT 'scheduled'
                          CHECK (status IN ('scheduled', 'cancelled', 'completed', 'postponed')),
    visibility        VARCHAR(30)         NOT NULL DEFAULT 'public'
                          CHECK (visibility IN ('public', 'members_only', 'internal')),
    capacity          INT,
    created_at        DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_calendar_event_org        FOREIGN KEY (org_id)     REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_calendar_event_created_by FOREIGN KEY (created_by) REFERENCES person (id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_calendar_event_org   ON calendar_event (org_id);
CREATE INDEX idx_calendar_event_start ON calendar_event (start_datetime);
CREATE INDEX idx_calendar_event_type  ON calendar_event (event_type);

-- Now that calendar_event exists, add the FK constraint to volunteer_hour
ALTER TABLE volunteer_hour
    ADD CONSTRAINT fk_volunteer_hour_event
    FOREIGN KEY (event_id) REFERENCES calendar_event (id) ON DELETE SET NULL;
