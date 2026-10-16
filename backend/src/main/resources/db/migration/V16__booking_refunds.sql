ALTER TABLE bookings
    ADD COLUMN refunded_at          TIMESTAMPTZ,
    ADD COLUMN refund_reference     VARCHAR(120),
    ADD COLUMN refund_amount_cents  INTEGER,
    ADD COLUMN cancelled_at         TIMESTAMPTZ,
    ADD COLUMN cancel_reason        VARCHAR(300);

CREATE INDEX idx_bookings_admin_list ON bookings(created_at DESC);
CREATE INDEX idx_bookings_lead_email ON bookings(lower(lead_email));