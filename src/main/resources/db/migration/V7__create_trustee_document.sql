-- V7__create_trustee_document.sql
-- Documents associated with a trustee (e.g. signed agreements, ID scans)

CREATE TABLE trustee_document (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trustee_id      UUID                NOT NULL REFERENCES trustee (id) ON DELETE CASCADE,
    document_type   VARCHAR(100)        NOT NULL,
    file_url        VARCHAR(500)        NOT NULL,
    uploaded_at     DATE                NOT NULL DEFAULT CURRENT_DATE,
    notes           TEXT
);

CREATE INDEX idx_trustee_document_trustee ON trustee_document (trustee_id);
