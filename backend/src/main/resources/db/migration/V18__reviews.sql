-- Verified reviews: one per booking, written by the customer after their trip,
-- shown publicly only once an admin approves it.
CREATE TABLE reviews (
    id              UUID PRIMARY KEY,
    booking_id      UUID         NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    tour_id         UUID         NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating          SMALLINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title           VARCHAR(120),
    body            TEXT         NOT NULL,
    author_name     VARCHAR(120) NOT NULL,
    author_country  VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    featured        BOOLEAN      NOT NULL DEFAULT FALSE,
    moderated_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    CHECK (NOT featured OR status = 'APPROVED')
);

CREATE INDEX idx_reviews_tour_status ON reviews(tour_id, status, created_at DESC);
CREATE INDEX idx_reviews_status ON reviews(status, created_at);

-- tours.rating and tours.review_count are now a cache computed from approved
-- reviews. The seeded values were placeholders, not real reviews.
UPDATE tours SET rating = 0.0, review_count = 0;
