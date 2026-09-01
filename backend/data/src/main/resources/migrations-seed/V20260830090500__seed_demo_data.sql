-- Demo content so a freshly started stack is immediately explorable.
-- Every date is relative to CURRENT_DATE, so the dashboard always shows live looking batches.

------------------------------------------------------------------------------------------------
-- Culture library, including a three generation SCOBY family tree.
------------------------------------------------------------------------------------------------
INSERT INTO cultures (id, name, ferment_type_id, kind, parent_culture_id, generation, acquired_on,
                      health, feed_interval_days, last_fed_on, flavor_profile, notes, active)
VALUES ('c1000000-0000-4000-8000-000000000001', 'Ada the SCOBY',
        'f7000000-0000-4000-8000-000000000001', 'SCOBY', NULL, 1, CURRENT_DATE - 730,
        'THRIVING', 14, CURRENT_DATE - 8, 'Clean apple acidity, light tannin',
        'Original pellicle, gifted by a neighbour. Never let it dry out.', true),
       ('c1000000-0000-4000-8000-000000000002', 'Ada II',
        'f7000000-0000-4000-8000-000000000001', 'SCOBY', 'c1000000-0000-4000-8000-000000000001', 2,
        CURRENT_DATE - 240, 'HEALTHY', 14, CURRENT_DATE - 5, 'Rounder, slightly more floral',
        'Split off Ada after a very thick pellicle formed.', true),
       ('c1000000-0000-4000-8000-000000000003', 'Ada III (hotel)',
        'f7000000-0000-4000-8000-000000000001', 'SCOBY', 'c1000000-0000-4000-8000-000000000002', 3,
        CURRENT_DATE - 60, 'HEALTHY', 21, CURRENT_DATE - 12, 'Untested, kept in the hotel',
        'Backup layer stored in the SCOBY hotel.', true),
       ('c1000000-0000-4000-8000-000000000004', 'Bertha',
        'f7000000-0000-4000-8000-000000000005', 'SOURDOUGH_STARTER', NULL, 1, CURRENT_DATE - 1460,
        'THRIVING', 1, CURRENT_DATE, 'Yoghurt tang with a wheaty finish',
        'Stiff 60% hydration starter, fed once a day at 26 °C.', true),
       ('c1000000-0000-4000-8000-000000000005', 'Bertha rye',
        'f7000000-0000-4000-8000-000000000005', 'SOURDOUGH_STARTER', 'c1000000-0000-4000-8000-000000000004', 2,
        CURRENT_DATE - 300, 'HEALTHY', 2, CURRENT_DATE - 3, 'Sharper, malty, distinctly rye',
        'Converted to wholegrain rye over a week of feeds.', true),
       ('c1000000-0000-4000-8000-000000000006', 'Milk kefir grains',
        'f7000000-0000-4000-8000-000000000003', 'KEFIR_GRAINS', NULL, 1, CURRENT_DATE - 500,
        'THRIVING', 1, CURRENT_DATE, 'Creamy, mildly yeasty',
        'Doubling every couple of weeks, split regularly.', true),
       ('c1000000-0000-4000-8000-000000000007', 'Water kefir grains',
        'f7000000-0000-4000-8000-000000000002', 'KEFIR_GRAINS', NULL, 1, CURRENT_DATE - 180,
        'SLUGGISH', 2, CURRENT_DATE - 6, 'Thin, needs mineral support',
        'Add a pinch of unrefined sugar and a dried fig to wake them up.', true),
       ('c1000000-0000-4000-8000-000000000008', 'Vinegar mother',
        'f7000000-0000-4000-8000-000000000013', 'VINEGAR_MOTHER', NULL, 1, CURRENT_DATE - 900,
        'HEALTHY', 30, CURRENT_DATE - 26, 'Sharp, sherry-like',
        'Lives in a 3 L crock topped up with leftover cider.', true),
       ('c1000000-0000-4000-8000-000000000009', 'Kimchi backslop',
        'f7000000-0000-4000-8000-000000000006', 'BRINE_BACKSLOP', NULL, 1, CURRENT_DATE - 45,
        'DORMANT', NULL, NULL, 'Salty, funky, garlicky',
        'Reserved brine from the last kimchi, used to kick off the next one.', true);

------------------------------------------------------------------------------------------------
-- Culture care log.
------------------------------------------------------------------------------------------------
INSERT INTO culture_feedings (id, culture_id, fed_at, amount, unit, note)
SELECT gen_random_uuid(),
       c.id,
       (c.last_fed_on - (offsets.n * COALESCE(c.feed_interval_days, 7)))::timestamptz + INTERVAL '8 hours',
       CASE c.kind WHEN 'SOURDOUGH_STARTER' THEN 60 WHEN 'KEFIR_GRAINS' THEN 500 ELSE 250 END,
       CASE c.kind WHEN 'SOURDOUGH_STARTER' THEN 'g' ELSE 'ml' END,
       CASE offsets.n WHEN 0 THEN 'Doubled in 5 hours, dome still holding.' ELSE NULL END
FROM cultures c
         CROSS JOIN (SELECT generate_series(0, 5) AS n) offsets
WHERE c.last_fed_on IS NOT NULL;

------------------------------------------------------------------------------------------------
-- Batches across the whole lifecycle: planned, active, overdue, completed and discarded.
------------------------------------------------------------------------------------------------
INSERT INTO batches (id, code, name, ferment_type_id, recipe_id, culture_id, status,
                     started_on, expected_end_on, ended_on, container, batch_size, batch_size_unit,
                     location, rating, outcome, notes)
VALUES ('b1000000-0000-4000-8000-000000000001', 'KOM-0001', 'Mango kombucha #12',
        'f7000000-0000-4000-8000-000000000001', 'a1000000-0000-4000-8000-000000000001',
        'c1000000-0000-4000-8000-000000000001', 'ACTIVE',
        CURRENT_DATE - 8, CURRENT_DATE + 4, NULL, '5 L glass jar', 4.000, 'L',
        'Pantry shelf', NULL, NULL, 'Pushed the sugar to 7% to see if it carbonates harder.'),

       ('b1000000-0000-4000-8000-000000000002', 'KIM-0002', 'Autumn napa kimchi',
        'f7000000-0000-4000-8000-000000000006', 'a1000000-0000-4000-8000-000000000002',
        'c1000000-0000-4000-8000-000000000009', 'ACTIVE',
        CURRENT_DATE - 5, CURRENT_DATE + 2, NULL, '3 L crock', 2.000, 'kg',
        'Cellar', NULL, NULL, 'Backslopped with the last batch brine for a faster start.'),

       ('b1000000-0000-4000-8000-000000000003', 'SAU-0003', 'Caraway sauerkraut',
        'f7000000-0000-4000-8000-000000000007', 'a1000000-0000-4000-8000-000000000003',
        NULL, 'ACTIVE',
        CURRENT_DATE - 25, CURRENT_DATE - 4, NULL, '5 L crock', 1.500, 'kg',
        'Cellar', NULL, NULL, 'Left running past the plan on purpose to test the sour limit.'),

       ('b1000000-0000-4000-8000-000000000004', 'HOT-0004', 'Habanero gold',
        'f7000000-0000-4000-8000-000000000009', 'a1000000-0000-4000-8000-000000000005',
        NULL, 'ACTIVE',
        CURRENT_DATE - 12, CURRENT_DATE + 6, NULL, '2 L fermenter', 1.000, 'kg',
        'Kitchen counter', NULL, NULL, 'Half habanero, half red jalapeno.'),

       ('b1000000-0000-4000-8000-000000000005', 'MEA-0005', 'Session mead 2026',
        'f7000000-0000-4000-8000-000000000011', 'a1000000-0000-4000-8000-000000000008',
        NULL, 'ACTIVE',
        CURRENT_DATE - 30, CURRENT_DATE + 15, NULL, '5 L carboy', 5.000, 'L',
        'Cellar', NULL, NULL, 'Wildflower honey from the spring harvest.'),

       ('b1000000-0000-4000-8000-000000000006', 'TEP-0006', 'Pineapple tepache',
        'f7000000-0000-4000-8000-000000000014', NULL,
        NULL, 'ACTIVE',
        CURRENT_DATE - 2, CURRENT_DATE + 1, NULL, '2 L jar', 1.800, 'L',
        'Kitchen counter', NULL, NULL, 'No recipe, straight rind and piloncillo.'),

       ('b1000000-0000-4000-8000-000000000007', 'KEF-0007', 'Daily milk kefir',
        'f7000000-0000-4000-8000-000000000003', 'a1000000-0000-4000-8000-000000000007',
        'c1000000-0000-4000-8000-000000000006', 'ACTIVE',
        CURRENT_DATE - 1, CURRENT_DATE, NULL, '1 L jar', 1.000, 'L',
        'Kitchen counter', NULL, NULL, NULL),

       ('b1000000-0000-4000-8000-000000000008', 'KOM-0008', 'Raspberry kombucha',
        'f7000000-0000-4000-8000-000000000001', 'a1000000-0000-4000-8000-000000000001',
        'c1000000-0000-4000-8000-000000000002', 'PLANNED',
        CURRENT_DATE + 3, CURRENT_DATE + 15, NULL, '5 L glass jar', 4.000, 'L',
        'Pantry shelf', NULL, NULL, 'Waiting for the current jar to free up.'),

       ('b1000000-0000-4000-8000-000000000009', 'SOU-0009', 'Saturday country loaf',
        'f7000000-0000-4000-8000-000000000005', 'a1000000-0000-4000-8000-000000000004',
        'c1000000-0000-4000-8000-000000000004', 'COMPLETED',
        CURRENT_DATE - 3, CURRENT_DATE - 1, CURRENT_DATE - 1, 'Dutch oven', 900.000, 'g',
        'Kitchen', 5, 'EXCELLENT', 'Best oven spring yet. Bulk at 25 °C for 4h30 was the difference.'),

       ('b1000000-0000-4000-8000-000000000010', 'PIC-0010', 'Half sour dills',
        'f7000000-0000-4000-8000-000000000008', 'a1000000-0000-4000-8000-000000000006',
        NULL, 'COMPLETED',
        CURRENT_DATE - 20, CURRENT_DATE - 10, CURRENT_DATE - 10, '3 L jar', 2.000, 'kg',
        'Cellar', 4, 'GOOD', 'Pulled at day ten. Crisp, could take a touch less salt.'),

       ('b1000000-0000-4000-8000-000000000011', 'KOM-0011', 'Ginger turmeric kombucha',
        'f7000000-0000-4000-8000-000000000001', 'a1000000-0000-4000-8000-000000000001',
        'c1000000-0000-4000-8000-000000000002', 'COMPLETED',
        CURRENT_DATE - 40, CURRENT_DATE - 28, CURRENT_DATE - 26, '5 L glass jar', 4.000, 'L',
        'Pantry shelf', 4, 'GOOD', 'Two days over the plan, noticeably sharper than #12.'),

       ('b1000000-0000-4000-8000-000000000012', 'SAU-0012', 'Purple kraut',
        'f7000000-0000-4000-8000-000000000007', 'a1000000-0000-4000-8000-000000000003',
        NULL, 'DISCARDED',
        CURRENT_DATE - 60, CURRENT_DATE - 39, CURRENT_DATE - 50, '5 L crock', 1.500, 'kg',
        'Cellar', 1, 'FAILED', 'Brine dropped below the weight and it grew mould. Discarded at day ten.');

------------------------------------------------------------------------------------------------
-- Batch timelines, generated from the recipe steps the batch follows.
------------------------------------------------------------------------------------------------
INSERT INTO batch_stages (id, batch_id, ordinal, name, phase, instruction, planned_duration_days)
SELECT gen_random_uuid(), b.id, s.ordinal, s.title, s.phase, s.instruction, s.duration_days
FROM batches b
         JOIN recipe_steps s ON s.recipe_id = b.recipe_id;

-- The one batch without a recipe gets a hand written timeline.
INSERT INTO batch_stages (id, batch_id, ordinal, name, phase, instruction, planned_duration_days)
VALUES (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000006', 1, 'Pack rind and sugar', 'PREP',
        'Cover the pineapple rind with water and dissolve the piloncillo.', 0),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000006', 2, 'Wild ferment', 'PRIMARY',
        'Cover with cloth and leave on the counter. Skim the foam daily.', 3),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000006', 3, 'Strain and chill', 'COLD_STORAGE',
        'Strain into bottles and refrigerate.', 0);

-- Prep work is done the moment a batch starts.
UPDATE batch_stages st
SET started_at   = b.started_on::timestamptz + INTERVAL '9 hours',
    completed_at = b.started_on::timestamptz + INTERVAL '11 hours'
FROM batches b
WHERE st.batch_id = b.id
  AND st.phase = 'PREP'
  AND b.status <> 'PLANNED';

-- The primary ferment is running on every batch that has started.
UPDATE batch_stages st
SET started_at = b.started_on::timestamptz + INTERVAL '11 hours'
FROM batches b
WHERE st.batch_id = b.id
  AND st.phase = 'PRIMARY'
  AND b.status <> 'PLANNED';

-- Everything is closed on a finished batch.
UPDATE batch_stages st
SET started_at   = COALESCE(st.started_at, b.started_on::timestamptz + INTERVAL '11 hours'),
    completed_at = b.ended_on::timestamptz + INTERVAL '17 hours'
FROM batches b
WHERE st.batch_id = b.id
  AND b.ended_on IS NOT NULL;

------------------------------------------------------------------------------------------------
-- Measurement series. pH walks from "just pitched" down towards the healthy floor for the style,
-- temperature wobbles inside the recommended window, Brix falls as the sugar is eaten.
------------------------------------------------------------------------------------------------
WITH tracked AS (SELECT b.id                                                      AS batch_id,
                        b.started_on,
                        LEAST(COALESCE(b.ended_on, CURRENT_DATE), CURRENT_DATE)   AS through_date,
                        ft.category,
                        COALESCE(ft.target_ph_max, 4.20) + 1.40                   AS start_ph,
                        COALESCE(ft.target_ph_min, 3.20)                          AS end_ph,
                        COALESCE(ft.target_temp_min_c, 19.00)                     AS temp_min,
                        COALESCE(ft.target_temp_max_c, 24.00)                     AS temp_max
                 FROM batches b
                          JOIN ferment_types ft ON ft.id = b.ferment_type_id
                 WHERE b.status <> 'PLANNED'),
     series AS (SELECT t.*,
                       gs.day_offset,
                       GREATEST(t.through_date - t.started_on, 1) AS span
                FROM tracked t
                         CROSS JOIN LATERAL generate_series(0, GREATEST(t.through_date - t.started_on, 0)) AS gs(day_offset))
INSERT
INTO measurements (id, batch_id, metric, metric_value, unit, recorded_at, note)
SELECT gen_random_uuid(),
       s.batch_id,
       'PH',
       ROUND(s.start_ph - (s.start_ph - s.end_ph) * (s.day_offset::numeric / s.span), 2),
       'pH',
       (s.started_on + s.day_offset)::timestamptz + INTERVAL '9 hours',
       NULL
FROM series s
WHERE s.day_offset % 2 = 0
   OR s.day_offset = s.span;

WITH tracked AS (SELECT b.id                                                    AS batch_id,
                        b.started_on,
                        LEAST(COALESCE(b.ended_on, CURRENT_DATE), CURRENT_DATE) AS through_date,
                        COALESCE(ft.target_temp_min_c, 19.00)                   AS temp_min,
                        COALESCE(ft.target_temp_max_c, 24.00)                   AS temp_max
                 FROM batches b
                          JOIN ferment_types ft ON ft.id = b.ferment_type_id
                 WHERE b.status <> 'PLANNED'),
     series AS (SELECT t.*, gs.day_offset
                FROM tracked t
                         CROSS JOIN LATERAL generate_series(0, GREATEST(t.through_date - t.started_on, 0)) AS gs(day_offset))
INSERT
INTO measurements (id, batch_id, metric, metric_value, unit, recorded_at, note)
SELECT gen_random_uuid(),
       s.batch_id,
       'TEMPERATURE',
       -- SIN() returns double precision, so the result is cast back before rounding.
       ROUND((s.temp_min + (s.temp_max - s.temp_min) * (0.35 + 0.30 * SIN(s.day_offset)))::numeric, 1),
       '°C',
       (s.started_on + s.day_offset)::timestamptz + INTERVAL '9 hours',
       NULL
FROM series s;

WITH tracked AS (SELECT b.id                                                    AS batch_id,
                        b.started_on,
                        LEAST(COALESCE(b.ended_on, CURRENT_DATE), CURRENT_DATE) AS through_date
                 FROM batches b
                          JOIN ferment_types ft ON ft.id = b.ferment_type_id
                 WHERE b.status <> 'PLANNED'
                   AND ft.category = 'BEVERAGE'),
     series AS (SELECT t.*,
                       gs.day_offset,
                       GREATEST(t.through_date - t.started_on, 1) AS span
                FROM tracked t
                         CROSS JOIN LATERAL generate_series(0, GREATEST(t.through_date - t.started_on, 0)) AS gs(day_offset))
INSERT
INTO measurements (id, batch_id, metric, metric_value, unit, recorded_at, note)
SELECT gen_random_uuid(),
       s.batch_id,
       'BRIX',
       ROUND(7.60 - 5.40 * (s.day_offset::numeric / s.span), 1),
       '°Bx',
       (s.started_on + s.day_offset)::timestamptz + INTERVAL '10 hours',
       NULL
FROM series s
WHERE s.day_offset % 3 = 0
   OR s.day_offset = s.span;

-- Hand placed readings that must be the newest on their batch, so they are timestamped one hour
-- after whatever the generated series produced. Timestamping them relative to `now()` would be
-- fragile: the generated points are stamped at 09:00 and 10:00, so a morning start-up would leave
-- them in the future and the "newest reading" checks would pick the wrong row.
INSERT INTO measurements (id, batch_id, metric, metric_value, unit, recorded_at, note)
SELECT gen_random_uuid(),
       v.batch_id,
       v.metric,
       v.metric_value,
       v.unit,
       COALESCE(
               (SELECT MAX(m.recorded_at) FROM measurements m WHERE m.batch_id = v.batch_id),
               now()
       ) + (v.hours_after || ' hours')::interval,
       v.note
FROM (VALUES
          -- The overdue sauerkraut has been pushed past its sour limit, which is what trips the
          -- over-fermentation detection on the dashboard and raises a critical alert.
          ('b1000000-0000-4000-8000-000000000003'::uuid, 'PH', 3.02, 'pH', 1,
           'Sharper than I want. Time to jar it.'),
          ('b1000000-0000-4000-8000-000000000003'::uuid, 'SALINITY', 2.00, '%', 1, NULL),
          ('b1000000-0000-4000-8000-000000000005'::uuid, 'SPECIFIC_GRAVITY', 1.0060, 'SG', 1,
           'Almost dry.'),
          ('b1000000-0000-4000-8000-000000000005'::uuid, 'ABV', 5.60, '%', 1, NULL),
          ('b1000000-0000-4000-8000-000000000004'::uuid, 'SALINITY', 3.50, '%', 1, NULL),
          ('b1000000-0000-4000-8000-000000000009'::uuid, 'HYDRATION', 78.00, '%', 1, NULL)
     ) AS v(batch_id, metric, metric_value, unit, hours_after, note);

------------------------------------------------------------------------------------------------
-- Journal entries.
------------------------------------------------------------------------------------------------
INSERT INTO batch_events (id, batch_id, event_type, occurred_at, title, description)
VALUES (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000001', 'OBSERVATION',
        (CURRENT_DATE - 6)::timestamptz + INTERVAL '9 hours', 'New pellicle forming',
        'Thin white film across the whole surface, no signs of mould.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000001', 'TASTING',
        (CURRENT_DATE - 2)::timestamptz + INTERVAL '18 hours', 'Day 6 taste',
        'Still noticeably sweet. Giving it another three days before the second ferment.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000002', 'OBSERVATION',
        (CURRENT_DATE - 4)::timestamptz + INTERVAL '20 hours', 'Active bubbling',
        'Brine rose over the weight overnight, had to wipe the rim.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000002', 'TASTING',
        (CURRENT_DATE - 1)::timestamptz + INTERVAL '13 hours', 'Day 4 taste',
        'Bright and fizzy on the tongue, sourness only just arriving.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000003', 'ISSUE',
        (CURRENT_DATE - 5)::timestamptz + INTERVAL '11 hours', 'Kahm yeast on the surface',
        'Skimmed off a thin wrinkled film. Brine still covering the cabbage.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000003', 'TASTING',
        now() - INTERVAL '3 hours', 'Past the sour line',
        'Very sharp now. Jarring it today before it goes further.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000004', 'BURPING',
        (CURRENT_DATE - 9)::timestamptz + INTERVAL '8 hours', 'Airlock very active',
        'Steady bubbling, roughly one per second.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000005', 'OBSERVATION',
        (CURRENT_DATE - 16)::timestamptz + INTERVAL '19 hours', 'Racked off the lees',
        'Clean transfer, about 200 ml of loss.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000009', 'HARVEST',
        (CURRENT_DATE - 1)::timestamptz + INTERVAL '17 hours', 'Baked',
        'Huge oven spring, open crumb, blistered crust.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000010', 'HARVEST',
        (CURRENT_DATE - 10)::timestamptz + INTERVAL '16 hours', 'Jarred',
        'Moved to the fridge at full sour.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000011', 'TASTING',
        (CURRENT_DATE - 26)::timestamptz + INTERVAL '18 hours', 'Bottled',
        'Sharper than planned but the ginger carries it.'),
       (gen_random_uuid(), 'b1000000-0000-4000-8000-000000000012', 'ISSUE',
        (CURRENT_DATE - 50)::timestamptz + INTERVAL '12 hours', 'Mould, batch lost',
        'Fuzzy blue growth above the brine line. Whole crock discarded.');
