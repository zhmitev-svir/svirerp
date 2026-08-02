-- V44__add_zelle_facebook_payment_methods.sql
-- Adds 'zelle' and 'facebook' as recordable payment methods, same treatment 'stripe' got in V43.
-- The three new "Undeposited Funds" clearing accounts (1020/1021/1022) need no migration of their
-- own: they're picked up by FinanceService#DEFAULT_ACCOUNTS for new orgs, and retrofitted for the
-- existing org via FinanceService#findOrCreateAccountByNumber the same way account 5320 was in V43.

ALTER TABLE journal_entry
    MODIFY COLUMN payment_method VARCHAR(30)
        CHECK (payment_method IN ('cash', 'check', 'zeffy', 'bank_transfer', 'card', 'other', 'stripe', 'zelle', 'facebook'));
