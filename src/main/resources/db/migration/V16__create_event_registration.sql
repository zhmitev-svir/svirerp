-- V16__create_event_registration.sql
-- Tracks person registrations for calendar events.

CREATE TABLE event_registration (
    id              CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    event_id        CHAR(36)            NOT NULL,
    person_id       CHAR(36)            NOT NULL,
    registered_at   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(30)         NOT NULL DEFAULT 'registered'
                        CHECK (status IN ('registered', 'attended', 'cancelled', 'waitlisted', 'no_show')),
    fee_paid        DECIMAL(10, 2)      DEFAULT 0.00,
    ticket_number   VARCHAR(50)         UNIQUE,
    notes           TEXT,

    CONSTRAINT uq_event_registration     UNIQUE (event_id, person_id),
    CONSTRAINT fk_event_registration_event  FOREIGN KEY (event_id)  REFERENCES calendar_event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_registration_person FOREIGN KEY (person_id) REFERENCES person (id)         ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_event_registration_event  ON event_registration (event_id);
CREATE INDEX idx_event_registration_person ON event_registration (person_id);
