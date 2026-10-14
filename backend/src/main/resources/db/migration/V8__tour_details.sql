CREATE TABLE tour_itinerary_days (
    id              UUID PRIMARY KEY,
    tour_id         UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    day_number      INTEGER NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT NOT NULL,
    accommodation   VARCHAR(200),
    meals_included  VARCHAR(200),
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    UNIQUE (tour_id, day_number)
);

CREATE INDEX idx_tour_itinerary_tour ON tour_itinerary_days(tour_id, day_number);

CREATE TABLE tour_inclusions (
    id              UUID PRIMARY KEY,
    tour_id         UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    label           VARCHAR(300) NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tour_inclusions_tour ON tour_inclusions(tour_id, display_order);

CREATE TABLE tour_images (
    id              UUID PRIMARY KEY,
    tour_id         UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    image_url       VARCHAR(500) NOT NULL,
    alt_text        VARCHAR(200),
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tour_images_tour ON tour_images(tour_id, display_order);