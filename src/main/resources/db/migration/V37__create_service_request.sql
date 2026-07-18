-- V37__create_service_request.sql
-- Pre-paid church services (weddings, baptisms, funerals, memorials) tracked independently of
-- scheduling so a deposit can be recorded before a calendar date is confirmed.

CREATE TABLE service_request (
    id                  CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id              CHAR(36)            NOT NULL,
    requestor_person_id CHAR(36),
    service_type        VARCHAR(50)         NOT NULL
                             CHECK (service_type IN ('wedding', 'baptism', 'funeral', 'memorial', 'blessing', 'other')),
    requested_date       DATE,
    agreed_amount        DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    status                VARCHAR(30)         NOT NULL DEFAULT 'requested'
                             CHECK (status IN ('requested', 'scheduled', 'completed', 'cancelled')),
    church_event_id       CHAR(36),
    notes                 TEXT,
    created_at            DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_service_request_org       FOREIGN KEY (org_id)              REFERENCES organization (id)  ON DELETE CASCADE,
    CONSTRAINT fk_service_request_requestor FOREIGN KEY (requestor_person_id) REFERENCES person (id)        ON DELETE SET NULL,
    CONSTRAINT fk_service_request_event     FOREIGN KEY (church_event_id)     REFERENCES church_event (id)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_service_request_org    ON service_request (org_id);
CREATE INDEX idx_service_request_status ON service_request (status);
