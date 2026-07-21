-- V40__widen_member_payment_payment_method_check.sql
-- Zeffy-sourced payments need their own payment_method value so they're distinguishable from
-- manually-entered dues payments. On MariaDB 11.8 this CHECK is embedded directly in the column
-- definition (not a droppable table-level constraint despite information_schema listing it as
-- one) — confirmed against the dev DB that both "DROP CONSTRAINT payment_method" and
-- "DROP CHECK payment_method" fail with syntax/existence errors here. MODIFY COLUMN redefines the
-- whole column, including its CHECK, in one atomic statement and is confirmed to work.

ALTER TABLE member_payment
    MODIFY COLUMN payment_method VARCHAR(50)
        CHECK (payment_method IN ('cash', 'check', 'credit_card', 'ach', 'online', 'other', 'zeffy'));
