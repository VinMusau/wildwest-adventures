CREATE TABLE availability (
    id              UUID PRIMARY KEY,
    tour_id         UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    departure_date  DATE NOT NULL,
    capacity        INTEGER NOT NULL,
    booked          INTEGER NOT NULL DEFAULT 0,
    price_cents     INTEGER,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    UNIQUE (tour_id, departure_date),
    CHECK (capacity > 0),
    CHECK (booked >= 0 AND booked <= capacity),
    CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED'))
);

CREATE INDEX idx_availability_tour_date ON availability(tour_id, departure_date);
CREATE INDEX idx_availability_status ON availability(status);