-- V43__add_stripe_fee_and_payment_processing_fees_account.sql
-- Stripe deducts its processing fee before ever depositing to the bank, so the amount recorded as
-- income (what the payer actually gave) is now more than what lands in Checking. stripe_webhook_event
-- gets a `fee` column (from Stripe's BalanceTransaction — see StripeWebhookService#resolveBalanceTransaction)
-- purely for audit/display; the actual accounting split (deposit = net, plus a fee expense line) is
-- built by FinanceService#recordIncome using a lazily-created "Payment Processing Fees" account —
-- see FinanceService#findOrCreateAccountByNumber, since this org already has an established chart
-- of accounts and the normal DEFAULT_ACCOUNTS seed only runs for a brand-new org with none yet.

ALTER TABLE stripe_webhook_event
    ADD COLUMN fee DECIMAL(15, 2);
