INSERT INTO availability (id, tour_id, departure_date, capacity, booked, price_cents, currency, status, created_at, updated_at, version)
SELECT
    gen_random_uuid(),
    t.id,
    d::date,
    8,
    CASE
        WHEN EXTRACT(DAY FROM d) IN (15, 22) THEN 8  -- simulate sold out
        WHEN EXTRACT(DAY FROM d) IN (8)      THEN 5  -- simulate partial
        ELSE 0
    END,
    t.price_cents,
    t.currency,
    'OPEN',
    now(), now(), 0
FROM tours t
CROSS JOIN generate_series(
    (date_trunc('week', now()) + interval '7 days')::date,
    (date_trunc('week', now()) + interval '180 days')::date,
    interval '7 days'
) AS d
WHERE t.slug <> 'great-migration'
UNION ALL
-- Great Migration gets two departures per week (Tuesdays and Fridays)
SELECT
    gen_random_uuid(),
    t.id,
    d::date,
    6,
    0,
    t.price_cents,
    t.currency,
    'OPEN',
    now(), now(), 0
FROM tours t
CROSS JOIN generate_series(
    (date_trunc('week', now()) + interval '8 days')::date,
    (date_trunc('week', now()) + interval '180 days')::date,
    interval '3 days'
) AS d
WHERE t.slug = 'great-migration';