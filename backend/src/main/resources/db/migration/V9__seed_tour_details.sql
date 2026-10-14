-- ----------------------------------------------------------------------------
-- 1. Generic 4-day itinerary for every tour EXCEPT the Great Migration
-- ----------------------------------------------------------------------------
INSERT INTO tour_itinerary_days (id, tour_id, day_number, title, description, created_at, updated_at, version)
SELECT
    gen_random_uuid(),
    t.id,
    d.day_num,
    CASE d.day_num
        WHEN 1 THEN 'Arrival & welcome'
        WHEN 2 THEN 'Into the wild'
        WHEN 3 THEN 'A day to remember'
        WHEN 4 THEN 'Farewell'
    END,
    CASE d.day_num
        WHEN 1 THEN 'Arrive at your destination and settle into your accommodation. Your guide will meet you for a welcome briefing over dinner.'
        WHEN 2 THEN 'A full day exploring the surrounding landscape with your private guide. Return to camp as the light fades.'
        WHEN 3 THEN 'Another unhurried day of activities shaped around the wildlife, the weather and your own curiosity.'
        WHEN 4 THEN 'A final morning at leisure before your transfer to the airport and journey home.'
    END,
    now(), now(), 0
FROM tours t
CROSS JOIN (VALUES (1), (2), (3), (4)) AS d(day_num)
WHERE t.slug <> 'great-migration';

-- ----------------------------------------------------------------------------
-- 2. Detailed 12-day itinerary for the Great Migration
-- ----------------------------------------------------------------------------
INSERT INTO tour_itinerary_days (id, tour_id, day_number, title, description, accommodation, meals_included, created_at, updated_at, version) VALUES
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 1, 'Arrival in Arusha', 'Settle into a peaceful coffee estate beneath Mount Meru. Your guide will meet you for a welcome briefing over dinner.', 'Legendary Lodge, Arusha', 'Dinner', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 2, 'Into the Serengeti', 'Fly into the wild heart of the Serengeti for your first game drive. The plains open around you like a story beginning.', 'Singita Sasakwa Lodge', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 3, 'Following the herds', 'A full day tracking the migration with your private naturalist. Somewhere ahead, half a million wildebeest are moving.', 'Singita Sasakwa Lodge', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 4, 'The great plains', 'Dawn and dusk game drives. In between, long lunches under acacia trees and an afternoon at leisure by the pool.', 'Singita Sasakwa Lodge', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 5, 'North to the Mara River', 'Cross into the northern Serengeti. This is where the drama unfolds each year, when the herds brave the crocodile-filled waters.', 'Sayari Camp', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 6, 'River crossings', 'A full day at the river, waiting with patience and quiet. When the crossing begins, you will not forget it.', 'Sayari Camp', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 7, 'The Maasai Mara', 'Cross into Kenya and settle into a secluded riverside camp. The afternoon is yours.', 'Angama Mara', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 8, 'Big cats of the Mara', 'The Maasai Mara has the densest population of lions, leopards and cheetahs in Africa. Today you go looking for them.', 'Angama Mara', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 9, 'A morning with the Maasai', 'Visit a Maasai village and learn about the traditions that have shaped this land for centuries.', 'Angama Mara', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 10, 'Hot-air balloon safari', 'Rise before dawn for a balloon flight over the Mara. Champagne breakfast on the plains when you land.', 'Angama Mara', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 11, 'Final game drive', 'One last dawn drive before returning to camp to pack. Afternoon at leisure and a farewell dinner.', 'Angama Mara', 'All meals', now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301', 12, 'Departure', 'Transfer to Nairobi for your international flight home, with the Serengeti still humming in your ears.', NULL, 'Breakfast', now(), now(), 0);

-- ----------------------------------------------------------------------------
-- 3. Inclusions for every tour
-- ----------------------------------------------------------------------------
INSERT INTO tour_inclusions (id, tour_id, label, display_order, created_at, updated_at, version)
SELECT gen_random_uuid(), t.id, v.label, v.ord, now(), now(), 0
FROM tours t
CROSS JOIN (VALUES
    ('Private guiding throughout', 1),
    ('Handpicked accommodation', 2),
    ('All internal flights and transfers', 3),
    ('Daily meals and selected drinks', 4),
    ('24/7 in-country support', 5)
) AS v(label, ord);

-- ----------------------------------------------------------------------------
-- 4. Gallery images for the three featured tours
-- ----------------------------------------------------------------------------
INSERT INTO tour_images (id, tour_id, image_url, alt_text, display_order, created_at, updated_at, version) VALUES

-- Great Migration
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301',
 'https://images.unsplash.com/photo-1577971132997-c10be9372519?auto=format&fit=crop&w=1600&q=85',
 'Elephants crossing the Serengeti', 1, now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301',
 'https://images.unsplash.com/photo-1765706729858-76ae4bcd2f41?auto=format&fit=crop&w=1600&q=85',
 'Safari game drive at golden hour', 2, now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333301',
 'https://images.unsplash.com/photo-1603483080228-04f2313d9f10?auto=format&fit=crop&w=1600&q=85',
 'Elephant on the savannah', 3, now(), now(), 0),

-- Zanzibar
(gen_random_uuid(), '33333333-3333-3333-3333-333333333302',
 'https://images.unsplash.com/photo-1634646350433-fe03ad698448?auto=format&fit=crop&w=1600&q=85',
 'Turquoise water off the coast', 1, now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333302',
 'https://images.unsplash.com/photo-1483683804023-6ccdb62f86ef?auto=format&fit=crop&w=1600&q=85',
 'Overwater villas', 2, now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333302',
 'https://images.unsplash.com/photo-1535941339077-2dd1c7963098?auto=format&fit=crop&w=1600&q=85',
 'Lodge veranda at dusk', 3, now(), now(), 0),

-- Patagonia
(gen_random_uuid(), '33333333-3333-3333-3333-333333333303',
 'https://images.unsplash.com/photo-1637580980556-085dee659c7e?auto=format&fit=crop&w=1600&q=85',
 'Patagonian peaks', 1, now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333303',
 'https://images.unsplash.com/photo-1494783329112-4a6795291178?auto=format&fit=crop&w=1600&q=85',
 'Mountain trail', 2, now(), now(), 0),
(gen_random_uuid(), '33333333-3333-3333-3333-333333333303',
 'https://images.unsplash.com/photo-1700455160321-88cbb18c25f7?auto=format&fit=crop&w=1600&q=85',
 'Open road at sunset', 3, now(), now(), 0);