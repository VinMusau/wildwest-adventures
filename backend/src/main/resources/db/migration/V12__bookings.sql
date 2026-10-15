CREATE TABLE bookings (
    id                        UUID PRIMARY KEY,
    reference                 VARCHAR(20) NOT NULL UNIQUE,
    user_id                   UUID NOT NULL REFERENCES users(id),
    tour_id                   UUID NOT NULL REFERENCES tours(id),
    availability_id           UUID NOT NULL REFERENCES availability(id),
    departure_date            DATE NOT NULL,
    travelers_count           INTEGER NOT NULL,
    price_cents_per_person    INTEGER NOT NULL,
    subtotal_cents            INTEGER NOT NULL,
    addons_total_cents        INTEGER NOT NULL DEFAULT 0,
    total_cents               INTEGER NOT NULL,
    currency                  VARCHAR(3) NOT NULL,
    status                    VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    lead_first_name           VARCHAR(100) NOT NULL,
    lead_last_name            VARCHAR(100) NOT NULL,
    lead_email                VARCHAR(255) NOT NULL,
    lead_phone                VARCHAR(50),
    lead_country              VARCHAR(100),
    expires_at                TIMESTAMPTZ,
    created_at                TIMESTAMPTZ NOT NULL,
    updated_at                TIMESTAMPTZ NOT NULL,
    version                   BIGINT NOT NULL DEFAULT 0,
    CHECK (travelers_count > 0),
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'))
);

CREATE INDEX idx_bookings_user       ON bookings(user_id, created_at DESC);
CREATE INDEX idx_bookings_reference  ON bookings(reference);
CREATE INDEX idx_bookings_status     ON bookings(status, expires_at);

CREATE TABLE booking_travelers (
    id              UUID PRIMARY KEY,
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    traveler_type   VARCHAR(20) NOT NULL DEFAULT 'ADULT',
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    CHECK (traveler_type IN ('ADULT', 'CHILD'))
);

CREATE INDEX idx_booking_travelers_booking ON booking_travelers(booking_id);

CREATE TABLE booking_addons (
    id              UUID PRIMARY KEY,
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    code            VARCHAR(60) NOT NULL,
    label           VARCHAR(200) NOT NULL,
    price_cents     INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_addons_booking ON booking_addons(booking_id);