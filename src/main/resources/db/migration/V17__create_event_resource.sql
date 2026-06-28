-- V17__create_event_resource.sql
-- Tracks resources (rooms, equipment, personnel) assigned to events

CREATE TABLE event_resource (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id       UUID                NOT NULL REFERENCES calendar_event (id) ON DELETE CASCADE,
    resource_type  VARCHAR(100)        NOT NULL,
    resource_name  VARCHAR(255)        NOT NULL,
    assigned_to    VARCHAR(255),
    notes          TEXT
);

CREATE INDEX idx_event_resource_event ON event_resource (event_id);
