-- V17__create_event_resource.sql
-- Tracks resources (rooms, equipment, personnel) assigned to events.

CREATE TABLE event_resource (
    id             CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    event_id       CHAR(36)            NOT NULL,
    resource_type  VARCHAR(100)        NOT NULL,
    resource_name  VARCHAR(255)        NOT NULL,
    assigned_to    VARCHAR(255),
    notes          TEXT,

    CONSTRAINT fk_event_resource_event FOREIGN KEY (event_id) REFERENCES calendar_event (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_event_resource_event ON event_resource (event_id);
