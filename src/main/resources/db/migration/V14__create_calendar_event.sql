-- V14__create_calendar_event.sql
-- Base event table for all organization events; church events extend this via V15

CREATE TABLE calendar_event (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    created_by        UUID                REFERENCES person (id) ON DELETE SET NULL,
    title             VARCHAR(255)        NOT NULL,
    description       TEXT,
    event_type        VARCHAR(100)        NOT NULL DEFAULT 'general',
    start_datetime    TIMESTAMP WITH TIME ZONE NOT NULL,
    end_datetime      TIMESTAMP WITH TIME ZONE,
    is_all_day        BOOLEAN             NOT NULL DEFAULT FALSE,
    location          VARCHAR(255),
    virtual_link      VARCHAR(500),
    is_recurring      BOOLEAN             NOT NULL DEFAULT FALSE,
    recurrence_rule   VARCHAR(255),
    status            VARCHAR(30)         NOT NULL DEFAULT 'scheduled'
                          CHECK (status IN ('scheduled', 'cancelled', 'completed', 'postponed')),
    visibility        VARCHAR(30)         NOT NULL DEFAULT 'public'
                          CHECK (visibility IN ('public', 'members_only', 'internal')),
    capacity          INTEGER,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_calendar_event_org   ON calendar_event (org_id);
CREATE INDEX idx_calendar_event_start ON calendar_event (start_datetime);
CREATE INDEX idx_calendar_event_type  ON calendar_event (event_type);

-- Now that calendar_event exists, add the FK constraint to volunteer_hour
ALTER TABLE volunteer_hour
    ADD CONSTRAINT fk_volunteer_hour_event
    FOREIGN KEY (event_id) REFERENCES calendar_event (id) ON DELETE SET NULL;
