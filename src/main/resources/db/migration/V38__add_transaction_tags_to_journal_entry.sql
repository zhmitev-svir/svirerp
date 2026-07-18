-- V38__add_transaction_tags_to_journal_entry.sql
-- Denormalized header tags for the simple Record Income/Record Expense flow, so the transaction
-- list can show category/fund/vendor/payer without joining journal_line. The real ledger truth
-- stays in journal_line (which already carries its own account_id/fund_id per line).

ALTER TABLE journal_entry
    ADD COLUMN payment_method      VARCHAR(30)
        CHECK (payment_method IN ('cash', 'check', 'zeffy', 'bank_transfer', 'card', 'other')),
    ADD COLUMN check_number        VARCHAR(20),
    ADD COLUMN payer_id            CHAR(36),
    ADD COLUMN vendor_id           CHAR(36),
    ADD COLUMN service_request_id  CHAR(36),
    ADD COLUMN category_account_id CHAR(36),
    ADD COLUMN fund_id             CHAR(36);

ALTER TABLE journal_entry
    ADD CONSTRAINT fk_journal_entry_payer            FOREIGN KEY (payer_id)            REFERENCES person (id)          ON DELETE SET NULL,
    ADD CONSTRAINT fk_journal_entry_vendor           FOREIGN KEY (vendor_id)           REFERENCES vendor (id)          ON DELETE SET NULL,
    ADD CONSTRAINT fk_journal_entry_service_request  FOREIGN KEY (service_request_id)  REFERENCES service_request (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_journal_entry_category_account FOREIGN KEY (category_account_id) REFERENCES account (id)         ON DELETE SET NULL,
    ADD CONSTRAINT fk_journal_entry_fund             FOREIGN KEY (fund_id)             REFERENCES fund (id)            ON DELETE SET NULL;

CREATE INDEX idx_journal_entry_service_request ON journal_entry (service_request_id);
CREATE INDEX idx_journal_entry_fund            ON journal_entry (fund_id);
