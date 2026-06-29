-- V5__create_member_payment.sql
-- Records each dues or fee payment made by a member.

CREATE TABLE member_payment (
    id               CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    member_id        CHAR(36)            NOT NULL,
    amount           DECIMAL(10, 2)      NOT NULL,
    payment_date     DATE                NOT NULL,
    payment_method   VARCHAR(50)         CHECK (payment_method IN ('cash', 'check', 'credit_card', 'ach', 'online', 'other')),
    transaction_ref  VARCHAR(100),
    period_start     DATE,
    period_end       DATE,
    status           VARCHAR(30)         NOT NULL DEFAULT 'completed'
                         CHECK (status IN ('pending', 'completed', 'failed', 'refunded')),
    notes            TEXT,

    CONSTRAINT fk_member_payment_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_member_payment_member ON member_payment (member_id);
CREATE INDEX idx_member_payment_date   ON member_payment (payment_date);
