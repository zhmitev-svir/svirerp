-- V15__create_church_event.sql
-- Extends calendar_event with worship-specific fields via a one-to-one relationship

CREATE TABLE church_event (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_event_id     UUID                NOT NULL UNIQUE REFERENCES calendar_event (id) ON DELETE CASCADE,
    service_type          VARCHAR(100),
    liturgical_season     VARCHAR(100),
    officiant             VARCHAR(255),
    sermon_title          VARCHAR(255),
    scripture_readings    TEXT,
    music_selections      TEXT,
    special_instructions  TEXT,
    offering_collected    NUMERIC(10, 2)      DEFAULT 0.00,
    attendance_count      INTEGER             DEFAULT 0
);

CREATE INDEX idx_church_event_calendar ON church_event (calendar_event_id);
