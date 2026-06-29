-- V9__create_volunteer_hour.sql
-- Logs service hours per volunteer per event for grant reporting and recognition.
-- event_id FK is intentionally omitted here; it is added in V14 after calendar_event exists.

CREATE TABLE volunteer_hour (
    id                    CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    volunteer_id          CHAR(36)            NOT NULL,
    event_id              CHAR(36),
    log_date              DATE                NOT NULL,
    hours                 DECIMAL(5, 2)       NOT NULL CHECK (hours > 0),
    activity_description  TEXT,
    approved_by           VARCHAR(255),
    status                VARCHAR(30)         NOT NULL DEFAULT 'pending'
                              CHECK (status IN ('pending', 'approved', 'rejected')),

    CONSTRAINT fk_volunteer_hour_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteer (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_volunteer_hour_volunteer ON volunteer_hour (volunteer_id);
CREATE INDEX idx_volunteer_hour_date      ON volunteer_hour (log_date);
