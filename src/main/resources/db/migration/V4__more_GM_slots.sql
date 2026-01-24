-- Extra available General Medicine slots in multiple facilities
-- Purpose: tests can verify filtering across facilities without inserting slots dynamically.

-- 1) Add an additional clinic in Harbor district (so GM exists in more than one place)
--    Safe because district names are unique from V2.
INSERT INTO facility(name, type, district_id)
SELECT 'Harbor Community Clinic', 'CLINIC', d.id
FROM district d
WHERE d.name = 'Harbor'
    ON CONFLICT (name) DO NOTHING;

-- Optional: add GM staff for the new clinic (not required for slots, but looks realistic)
INSERT INTO staff(facility_id, specialty_id, handle, active)
SELECT f.id, s.id, 'doc_harbor_01', TRUE
FROM facility f
         JOIN specialty s ON s.name = 'General Medicine'
WHERE f.name = 'Harbor Community Clinic'
    ON CONFLICT (facility_id, handle) DO NOTHING;

-- 2) Add available GM slots to multiple facilities.
--    No bookings are added for these slots -> they are available by definition.

-- Mobile Pod #7 (Neon Heights)
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity) VALUES
                                                                                          (
                                                                                              (SELECT f.id FROM facility f WHERE f.name = 'NeonCare Mobile Pod #7'),
                                                                                              (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
                                                                                              '2026-01-20 13:00:00+00', '2026-01-20 13:20:00+00', 1
                                                                                          ),
                                                                                          (
                                                                                              (SELECT f.id FROM facility f WHERE f.name = 'NeonCare Mobile Pod #7'),
                                                                                              (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
                                                                                              '2026-01-20 13:20:00+00', '2026-01-20 13:40:00+00', 2
                                                                                          );

-- Harbor Community Clinic (Harbor)
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity) VALUES
                                                                                          (
                                                                                              (SELECT f.id FROM facility f WHERE f.name = 'Harbor Community Clinic'),
                                                                                              (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
                                                                                              '2026-01-20 14:00:00+00', '2026-01-20 14:20:00+00', 1
                                                                                          ),
                                                                                          (
                                                                                              (SELECT f.id FROM facility f WHERE f.name = 'Harbor Community Clinic'),
                                                                                              (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
                                                                                              '2026-01-20 14:20:00+00', '2026-01-20 14:40:00+00', 1
                                                                                          );

-- Downtown General Hospital (Downtown) also offers GM slots (even if it’s mostly specialist care)
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity) VALUES
    (
        (SELECT f.id FROM facility f WHERE f.name = 'Downtown General Hospital'),
        (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
        '2026-01-20 15:00:00+00', '2026-01-20 15:20:00+00', 2
    );