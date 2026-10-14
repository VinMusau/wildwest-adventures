INSERT INTO tours (id, slug, title, place, destination_id, duration_days, price_cents, currency, rating, review_count, tag, image_url, featured, display_order, created_at, updated_at, version) VALUES

('33333333-3333-3333-3333-333333333301', 'great-migration', 'The Great Migration, Privately', 'Tanzania & Kenya',
 '22222222-2222-2222-2222-222222222201', 12, 985000, 'USD', 4.9, 124, 'Signature journey',
 'https://images.unsplash.com/photo-1577971132997-c10be9372519?auto=format&fit=crop&w=1200&q=85', TRUE, 1, now(), now(), 0),

('33333333-3333-3333-3333-333333333302', 'zanzibar-slow-island', 'Zanzibar: Slow Island Living', 'Tanzania',
 '22222222-2222-2222-2222-222222222201', 8, 524000, 'USD', 4.8, 86, 'Coastal escape',
 'https://images.unsplash.com/photo-1634646350433-fe03ad698448?auto=format&fit=crop&w=1200&q=85', TRUE, 2, now(), now(), 0),

('33333333-3333-3333-3333-333333333303', 'patagonia-beyond-map', 'Patagonia Beyond the Map', 'Chile & Argentina',
 '22222222-2222-2222-2222-222222222203', 11, 769000, 'USD', 4.9, 102, 'Small group',
 'https://images.unsplash.com/photo-1637580980556-085dee659c7e?auto=format&fit=crop&w=1200&q=85', TRUE, 3, now(), now(), 0),

('33333333-3333-3333-3333-333333333304', 'okavango-elephants', 'Elephants of the Okavango', 'Botswana',
 '22222222-2222-2222-2222-222222222202', 10, 842000, 'USD', 4.9, 74, 'Wildlife',
 'https://images.unsplash.com/photo-1603483080228-04f2313d9f10?auto=format&fit=crop&w=1200&q=85', FALSE, 4, now(), now(), 0),

('33333333-3333-3333-3333-333333333305', 'torres-del-paine', 'Torres del Paine on Foot', 'Chile',
 '22222222-2222-2222-2222-222222222203', 9, 628000, 'USD', 4.7, 61, 'Adventure',
 'https://images.unsplash.com/photo-1494783329112-4a6795291178?auto=format&fit=crop&w=1200&q=85', FALSE, 5, now(), now(), 0),

('33333333-3333-3333-3333-333333333306', 'wild-road-south', 'The Wild Road South', 'Argentina',
 '22222222-2222-2222-2222-222222222203', 14, 896000, 'USD', 4.9, 48, 'New',
 'https://images.unsplash.com/photo-1700455160321-88cbb18c25f7?auto=format&fit=crop&w=1200&q=85', FALSE, 6, now(), now(), 0),

('33333333-3333-3333-3333-333333333307', 'cape-town-winelands', 'Cape Town to the Winelands', 'South Africa',
 '22222222-2222-2222-2222-222222222202', 7, 418000, 'USD', 4.8, 93, 'Cultural',
 'https://images.unsplash.com/photo-1535941339077-2dd1c7963098?auto=format&fit=crop&w=1200&q=85', FALSE, 7, now(), now(), 0),

('33333333-3333-3333-3333-333333333308', 'maldives-private-island', 'Maldives Private Island', 'Maldives',
 '22222222-2222-2222-2222-222222222204', 6, 1120000, 'USD', 5.0, 42, 'Honeymoon',
 'https://images.unsplash.com/photo-1483683804023-6ccdb62f86ef?auto=format&fit=crop&w=1200&q=85', FALSE, 8, now(), now(), 0),

('33333333-3333-3333-3333-333333333309', 'seychelles-island-hopping', 'Seychelles Island Hopping', 'Seychelles',
 '22222222-2222-2222-2222-222222222204', 10, 940000, 'USD', 4.8, 56, 'Coastal',
 'https://images.unsplash.com/photo-1634646350433-fe03ad698448?auto=format&fit=crop&w=1200&q=85', FALSE, 9, now(), now(), 0),

('33333333-3333-3333-3333-333333333310', 'kenya-family-safari', 'Kenya Family Safari', 'Kenya',
 '22222222-2222-2222-2222-222222222201', 8, 650000, 'USD', 4.7, 38, 'Family',
 'https://images.unsplash.com/photo-1765706729858-76ae4bcd2f41?auto=format&fit=crop&w=1200&q=85', FALSE, 10, now(), now(), 0);

-- Tour ↔ travel style associations
INSERT INTO tour_styles (tour_id, style_id) VALUES
('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111101'), -- safari
('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111102'), -- coastal
('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111106'), -- honeymoon
('33333333-3333-3333-3333-333333333303', '11111111-1111-1111-1111-111111111104'), -- adventure
('33333333-3333-3333-3333-333333333303', '11111111-1111-1111-1111-111111111107'), -- small-group
('33333333-3333-3333-3333-333333333304', '11111111-1111-1111-1111-111111111101'), -- safari
('33333333-3333-3333-3333-333333333305', '11111111-1111-1111-1111-111111111104'), -- adventure
('33333333-3333-3333-3333-333333333306', '11111111-1111-1111-1111-111111111104'), -- adventure
('33333333-3333-3333-3333-333333333306', '11111111-1111-1111-1111-111111111107'), -- small-group
('33333333-3333-3333-3333-333333333307', '11111111-1111-1111-1111-111111111103'), -- cultural
('33333333-3333-3333-3333-333333333307', '11111111-1111-1111-1111-111111111105'), -- family
('33333333-3333-3333-3333-333333333308', '11111111-1111-1111-1111-111111111106'), -- honeymoon
('33333333-3333-3333-3333-333333333308', '11111111-1111-1111-1111-111111111102'), -- coastal
('33333333-3333-3333-3333-333333333309', '11111111-1111-1111-1111-111111111102'), -- coastal
('33333333-3333-3333-3333-333333333309', '11111111-1111-1111-1111-111111111106'), -- honeymoon
('33333333-3333-3333-3333-333333333310', '11111111-1111-1111-1111-111111111105'), -- family
('33333333-3333-3333-3333-333333333310', '11111111-1111-1111-1111-111111111101'); -- safari