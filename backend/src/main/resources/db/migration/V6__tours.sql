CREATE TABLE tours (
    id              UUID PRIMARY KEY,
    slug            VARCHAR(140) NOT NULL UNIQUE,
    title           VARCHAR(200) NOT NULL,
    place           VARCHAR(160) NOT NULL,
    destination_id  UUID         NOT NULL REFERENCES destinations(id),
    duration_days   INTEGER      NOT NULL,
    price_cents     INTEGER      NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'USD',
    rating          NUMERIC(2,1) NOT NULL DEFAULT 0.0,
    review_count    INTEGER      NOT NULL DEFAULT 0,
    tag             VARCHAR(60)  NOT NULL,
    image_url       VARCHAR(500) NOT NULL,
    featured        BOOLEAN      NOT NULL DEFAULT FALSE,
    display_order   INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_tours_slug            ON tours(slug);
CREATE INDEX idx_tours_destination     ON tours(destination_id);
CREATE INDEX idx_tours_price_cents     ON tours(price_cents);
CREATE INDEX idx_tours_duration_days   ON tours(duration_days);
CREATE INDEX idx_tours_display_order   ON tours(display_order);

CREATE TABLE tour_styles (
    tour_id   UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    style_id  UUID NOT NULL REFERENCES travel_styles(id) ON DELETE CASCADE,
    PRIMARY KEY (tour_id, style_id)
);

CREATE INDEX idx_tour_styles_style ON tour_styles(style_id);