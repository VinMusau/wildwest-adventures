CREATE TABLE destinations (
    id             UUID PRIMARY KEY,
    slug           VARCHAR(120) NOT NULL UNIQUE,
    name           VARCHAR(120) NOT NULL,
    image_url      VARCHAR(500) NOT NULL,
    journey_count  INTEGER      NOT NULL DEFAULT 0,
    display_order  INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_destinations_slug ON destinations(slug);
CREATE INDEX idx_destinations_display_order ON destinations(display_order);