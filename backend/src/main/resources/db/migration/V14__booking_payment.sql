ALTER TABLE bookings
    ADD COLUMN stripe_payment_intent_id VARCHAR(120),
    ADD COLUMN paid_at TIMESTAMPTZ,
    ADD COLUMN payment_method VARCHAR(40);

CREATE INDEX idx_bookings_payment_intent ON bookings(stripe_payment_intent_id);
CREATE INDEX idx_bookings_expires_at ON bookings(expires_at) WHERE status = 'PENDING';