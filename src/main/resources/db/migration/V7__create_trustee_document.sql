-- V7__create_trustee_document.sql
-- Documents associated with a trustee (e.g. signed agreements, ID scans).
-- DEFAULT (CURRENT_DATE) is an expression default, supported from MySQL 8.0.13+.

CREATE TABLE trustee_document (
    id              CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    trustee_id      CHAR(36)            NOT NULL,
    document_type   VARCHAR(100)        NOT NULL,
    file_url        VARCHAR(500)        NOT NULL,
    uploaded_at     DATE                NOT NULL DEFAULT (CURRENT_DATE),
    notes           TEXT,

    CONSTRAINT fk_trustee_document_trustee FOREIGN KEY (trustee_id) REFERENCES trustee (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_trustee_document_trustee ON trustee_document (trustee_id);
