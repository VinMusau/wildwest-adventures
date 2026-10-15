ALTER TABLE bookings
    ADD COLUMN paystack_reference VARCHAR(120),
    ADD COLUMN payment_provider VARCHAR(20) DEFAULT 'STRIPE';

CREATE INDEX idx_bookings_paystack_reference ON bookings(paystack_reference);

-- Existing bookings were paid via Stripe
UPDATE bookings SET payment_provider = 'STRIPE' WHERE payment_provider IS NULL;