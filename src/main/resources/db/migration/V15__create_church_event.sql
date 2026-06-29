-- V15__create_church_event.sql
-- Extends calendar_event with worship-specific fields via a one-to-one relationship.

CREATE TABLE church_event (
    id                    CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    calendar_event_id     CHAR(36)            NOT NULL UNIQUE,
    service_type          VARCHAR(100),
    liturgical_season     VARCHAR(100),
    officiant             VARCHAR(255),
    sermon_title          VARCHAR(255),
    scripture_readings    TEXT,
    music_selections      TEXT,
    special_instructions  TEXT,
    offering_collected    DECIMAL(10, 2)      DEFAULT 0.00,
    attendance_count      INT                 DEFAULT 0,

    CONSTRAINT fk_church_event_calendar FOREIGN KEY (calendar_event_id)
        REFERENCES calendar_event (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_church_event_calendar ON church_event (calendar_event_id);
