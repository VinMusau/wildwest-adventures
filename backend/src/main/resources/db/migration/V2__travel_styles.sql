CREATE TABLE travel_styles (
    id            UUID PRIMARY KEY,
    slug          VARCHAR(120) NOT NULL UNIQUE,
    title         VARCHAR(120) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    image_url     VARCHAR(500) NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_travel_styles_slug ON travel_styles(slug);
CREATE INDEX idx_travel_styles_display_order ON travel_styles(display_order);