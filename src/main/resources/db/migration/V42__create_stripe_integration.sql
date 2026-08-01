-- V42__create_stripe_integration.sql
-- Stripe webhook receiver: a Stripe Price -> purpose/Fund/Account mapping (so admins route new
-- products without code changes), and a durable log of every webhook event received, keyed by
-- Stripe's own event id for idempotency (Stripe redelivers on any non-2xx response, and can
-- occasionally duplicate a delivery outright). Checkout happens outside svirerp entirely
-- (WordPress / a mobile card-reader app) — this is purely a receiver that posts into the same
-- generic Fund/Account/MemberPayment/ServiceRequest/JournalEntry model the Zeffy import already
-- uses, one more payment_method value alongside it.

CREATE TABLE stripe_product_mapping (
    id                    CHAR(36)      PRIMARY KEY DEFAULT (UUID()),
    org_id                CHAR(36)      NOT NULL,
    stripe_price_id       VARCHAR(100)  NOT NULL,
    display_name          VARCHAR(255),
    purpose               VARCHAR(30)   NOT NULL
                              CHECK (purpose IN ('membership_dues', 'service_request', 'event_ticket', 'general_income')),
    fund_id               CHAR(36),
    category_account_id   CHAR(36),
    -- Only meaningful when purpose = 'service_request'; same allowed values as service_request.service_type.
    service_type          VARCHAR(50)
                              CHECK (service_type IS NULL OR service_type IN ('wedding', 'baptism', 'funeral', 'memorial', 'blessing', 'other')),
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_stripe_product_mapping_org_price UNIQUE (org_id, stripe_price_id),
    CONSTRAINT fk_stripe_product_mapping_org      FOREIGN KEY (org_id)              REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT fk_stripe_product_mapping_fund     FOREIGN KEY (fund_id)             REFERENCES fund (id)         ON DELETE SET NULL,
    CONSTRAINT fk_stripe_product_mapping_account  FOREIGN KEY (category_account_id) REFERENCES account (id)      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_stripe_product_mapping_org ON stripe_product_mapping (org_id);

CREATE TABLE stripe_webhook_event (
    id                  CHAR(36)      PRIMARY KEY DEFAULT (UUID()),
    org_id              CHAR(36)      NOT NULL,
    stripe_event_id     VARCHAR(255)  NOT NULL,
    event_type          VARCHAR(100)  NOT NULL,
    stripe_price_id     VARCHAR(100),
    amount              DECIMAL(15, 2),
    email               VARCHAR(255),
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    payload             LONGTEXT      NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'received'
                            CHECK (status IN ('received', 'processed', 'needs_mapping', 'error', 'ignored')),
    error_message       VARCHAR(500),
    received_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        DATETIME,

    -- Audit trail linking back to the records this event produced — same idea as zeffy_import_row.
    person_id           CHAR(36),
    member_id            CHAR(36),
    member_payment_id     CHAR(36),
    service_request_id     CHAR(36),
    journal_entry_id        CHAR(36),

    CONSTRAINT uq_stripe_webhook_event_stripe_id      UNIQUE (stripe_event_id),
    CONSTRAINT fk_stripe_webhook_event_org            FOREIGN KEY (org_id)             REFERENCES organization (id)    ON DELETE CASCADE,
    CONSTRAINT fk_stripe_webhook_event_person         FOREIGN KEY (person_id)          REFERENCES person (id)          ON DELETE SET NULL,
    CONSTRAINT fk_stripe_webhook_event_member         FOREIGN KEY (member_id)          REFERENCES member (id)          ON DELETE SET NULL,
    CONSTRAINT fk_stripe_webhook_event_member_payment FOREIGN KEY (member_payment_id)  REFERENCES member_payment (id)  ON DELETE SET NULL,
    CONSTRAINT fk_stripe_webhook_event_service_req    FOREIGN KEY (service_request_id) REFERENCES service_request (id) ON DELETE SET NULL,
    CONSTRAINT fk_stripe_webhook_event_journal_entry  FOREIGN KEY (journal_entry_id)   REFERENCES journal_entry (id)   ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_stripe_webhook_event_org    ON stripe_webhook_event (org_id);
CREATE INDEX idx_stripe_webhook_event_status ON stripe_webhook_event (status);

-- Widen the two existing payment_method CHECKs to add 'stripe' as its own value, distinguishable
-- from a manually-entered 'card' payment for reconciliation — same treatment 'zeffy' got in
-- V38/V40. MariaDB embeds a CHECK in the column definition itself (confirmed in V40's comment:
-- DROP CONSTRAINT/DROP CHECK both fail here), so MODIFY COLUMN is required.
ALTER TABLE member_payment
    MODIFY COLUMN payment_method VARCHAR(50)
        CHECK (payment_method IN ('cash', 'check', 'credit_card', 'ach', 'online', 'other', 'zeffy', 'stripe'));

ALTER TABLE journal_entry
    MODIFY COLUMN payment_method VARCHAR(30)
        CHECK (payment_method IN ('cash', 'check', 'zeffy', 'bank_transfer', 'card', 'other', 'stripe'));

-- Reuses the existing app_setting/AppSettingService/SettingEncryptor mechanism (same pattern as
-- gmail.oauth.client-id/secret) rather than a new secret-storage mechanism.
INSERT INTO app_setting (setting_key, value, value_type, description) VALUES
    ('stripe.secret-key', NULL, 'SECRET', 'Stripe API secret key (sk_live_... / sk_test_...) — used to call the Stripe API'),
    ('stripe.webhook-signing-secret', NULL, 'SECRET', 'Stripe webhook endpoint signing secret (whsec_...) — used to verify incoming webhook signatures');
