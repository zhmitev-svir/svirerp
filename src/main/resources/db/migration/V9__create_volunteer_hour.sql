-- V9__create_volunteer_hour.sql
-- Logs service hours per volunteer per event for grant reporting and recognition

CREATE TABLE volunteer_hour (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    volunteer_id          UUID                NOT NULL REFERENCES volunteer (id) ON DELETE RESTRICT,
    event_id              UUID,                                  -- FK added in V14 after calendar_event exists
    log_date              DATE                NOT NULL,
    hours                 NUMERIC(5, 2)       NOT NULL CHECK (hours > 0),
    activity_description  TEXT,
    approved_by           VARCHAR(255),
    status                VARCHAR(30)         NOT NULL DEFAULT 'pending'
                              CHECK (status IN ('pending', 'approved', 'rejected'))
);

CREATE INDEX idx_volunteer_hour_volunteer ON volunteer_hour (volunteer_id);
CREATE INDEX idx_volunteer_hour_date      ON volunteer_hour (log_date);
