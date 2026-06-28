-- V16__create_event_registration.sql
-- Tracks person registrations for calendar events

CREATE TABLE event_registration (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID                NOT NULL REFERENCES calendar_event (id) ON DELETE CASCADE,
    person_id       UUID                NOT NULL REFERENCES person (id) ON DELETE RESTRICT,
    registered_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status          VARCHAR(30)         NOT NULL DEFAULT 'registered'
                        CHECK (status IN ('registered', 'attended', 'cancelled', 'waitlisted', 'no_show')),
    fee_paid        NUMERIC(10, 2)      DEFAULT 0.00,
    ticket_number   VARCHAR(50)         UNIQUE,
    notes           TEXT,

    CONSTRAINT uq_event_registration UNIQUE (event_id, person_id)
);

CREATE INDEX idx_event_registration_event  ON event_registration (event_id);
CREATE INDEX idx_event_registration_person ON event_registration (person_id);
