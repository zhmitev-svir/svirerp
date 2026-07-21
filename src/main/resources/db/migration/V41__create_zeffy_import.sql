-- V41__create_zeffy_import.sql
-- Zeffy CSV import: campaign-title -> Fund mapping (persisted so recurring campaigns don't need
-- remapping every time), plus a batch/row audit trail that doubles as the preview-then-commit
-- staging area (rows are persisted at preview time with a per-row outcome, then only 'ready' rows
-- get applied on commit).

CREATE TABLE zeffy_campaign_mapping (
    id              CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id          CHAR(36)            NOT NULL,
    campaign_title  VARCHAR(255)        NOT NULL,
    fund_id         CHAR(36)            NOT NULL,
    created_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_zeffy_campaign_mapping_org_title UNIQUE (org_id, campaign_title),
    CONSTRAINT fk_zeffy_campaign_mapping_org  FOREIGN KEY (org_id)  REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_zeffy_campaign_mapping_fund FOREIGN KEY (fund_id) REFERENCES fund (id)         ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_zeffy_campaign_mapping_org ON zeffy_campaign_mapping (org_id);

CREATE TABLE zeffy_import_batch (
    id             CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id         CHAR(36)            NOT NULL,
    file_name      VARCHAR(255)        NOT NULL,
    uploaded_at    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status         VARCHAR(20)         NOT NULL DEFAULT 'previewed'
                       CHECK (status IN ('previewed', 'committed')),
    row_count      INT                 NOT NULL DEFAULT 0,
    committed_at   DATETIME,

    CONSTRAINT fk_zeffy_import_batch_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_zeffy_import_batch_org ON zeffy_import_batch (org_id);

CREATE TABLE zeffy_import_row (
    id                    CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    batch_id              CHAR(36)            NOT NULL,
    org_id                CHAR(36)            NOT NULL,
    -- "row_number" is a reserved word in MariaDB 10.2+ (ROW_NUMBER() window function) — confirmed
    -- against the dev DB, an unquoted column named row_number is a syntax error.
    csv_row_number        INT                 NOT NULL,

    -- Raw/parsed Zeffy CSV columns
    payment_date          DATE,
    payment_time          VARCHAR(20),
    amount                DECIMAL(10, 2),
    payment_status        VARCHAR(50),
    payout_date           DATE,
    first_name            VARCHAR(100),
    last_name             VARCHAR(100),
    email                 VARCHAR(255),
    address               VARCHAR(255),
    city                  VARCHAR(100),
    postal_code           VARCHAR(20),
    state                 VARCHAR(50),
    country                VARCHAR(100),
    tax_receipt_number     VARCHAR(50),
    tax_receipt_url        VARCHAR(500),
    campaign_title          VARCHAR(255),

    -- Computed during preview
    dedupe_key              VARCHAR(500),
    outcome                  VARCHAR(30)         NOT NULL DEFAULT 'pending_preview'
                                 CHECK (outcome IN ('pending_preview', 'ready', 'duplicate', 'skipped_status',
                                                     'unmapped_campaign', 'error', 'committed')),
    outcome_detail            VARCHAR(500),
    is_new_person             BOOLEAN             NOT NULL DEFAULT FALSE,
    is_new_member             BOOLEAN             NOT NULL DEFAULT FALSE,

    -- Stamped on commit (audit trail linking back to the records this row produced)
    person_id                 CHAR(36),
    member_id                 CHAR(36),
    member_payment_id         CHAR(36),
    journal_entry_id          CHAR(36),
    fund_id                   CHAR(36),

    CONSTRAINT fk_zeffy_import_row_batch          FOREIGN KEY (batch_id)         REFERENCES zeffy_import_batch (id) ON DELETE CASCADE,
    CONSTRAINT fk_zeffy_import_row_org            FOREIGN KEY (org_id)           REFERENCES organization (id)       ON DELETE CASCADE,
    CONSTRAINT fk_zeffy_import_row_person         FOREIGN KEY (person_id)        REFERENCES person (id)             ON DELETE SET NULL,
    CONSTRAINT fk_zeffy_import_row_member         FOREIGN KEY (member_id)        REFERENCES member (id)             ON DELETE SET NULL,
    CONSTRAINT fk_zeffy_import_row_member_payment FOREIGN KEY (member_payment_id) REFERENCES member_payment (id)    ON DELETE SET NULL,
    CONSTRAINT fk_zeffy_import_row_journal_entry  FOREIGN KEY (journal_entry_id) REFERENCES journal_entry (id)      ON DELETE SET NULL,
    CONSTRAINT fk_zeffy_import_row_fund           FOREIGN KEY (fund_id)          REFERENCES fund (id)               ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_zeffy_import_row_batch       ON zeffy_import_row (batch_id);
CREATE INDEX idx_zeffy_import_row_org         ON zeffy_import_row (org_id);
CREATE INDEX idx_zeffy_import_row_dedupe_key  ON zeffy_import_row (org_id, dedupe_key);
CREATE INDEX idx_zeffy_import_row_outcome     ON zeffy_import_row (outcome);
CREATE INDEX idx_zeffy_import_row_campaign    ON zeffy_import_row (campaign_title);
