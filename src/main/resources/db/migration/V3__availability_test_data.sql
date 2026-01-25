-- More appointment slots + bookings to test availability filtering
-- Goal: ensure some slots are fully booked (RESERVED/CONFIRMED == capacity) and must NOT be returned
-- while others have remaining capacity and SHOULD be returned.

-- ---------- Extra patients ----------
INSERT INTO patient(public_ref, created_at)
VALUES ('P-7K9L2', '2026-01-20 08:10:00+00'),
       ('P-0M3X8', '2026-01-20 08:12:00+00'),
       ('P-4J2QA', '2026-01-20 08:15:00+00'),
       ('P-9T1RN', '2026-01-20 08:18:00+00'),
       ('P-6C0PP', '2026-01-20 08:20:00+00');

-- ---------- Extra triage cases ----------
-- Use synthetic cases to attach bookings. Status doesn't matter for booking existence, but keep it realistic.
INSERT INTO triage_case(patient_id, intake_facility_id, required_specialty_id, severity, created_at, status)
VALUES ((SELECT p.id FROM patient p WHERE p.public_ref = 'P-7K9L2'),
        (SELECT f.id FROM facility f WHERE f.name = 'Kabuki Street Clinic'),
        (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
        3, '2026-01-20 08:25:00+00', 'WAITING'),
       ((SELECT p.id FROM patient p WHERE p.public_ref = 'P-0M3X8'),
        (SELECT f.id FROM facility f WHERE f.name = 'Kabuki Street Clinic'),
        (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
        2, '2026-01-20 08:26:00+00', 'WAITING'),
       ((SELECT p.id FROM patient p WHERE p.public_ref = 'P-4J2QA'),
        (SELECT f.id FROM facility f WHERE f.name = 'Downtown General Hospital'),
        (SELECT s.id FROM specialty s WHERE s.name = 'Cardiology'),
        4, '2026-01-20 08:28:00+00', 'WAITING'),
       ((SELECT p.id FROM patient p WHERE p.public_ref = 'P-9T1RN'),
        (SELECT f.id FROM facility f WHERE f.name = 'Downtown General Hospital'),
        (SELECT s.id FROM specialty s WHERE s.name = 'Radiology'),
        3, '2026-01-20 08:29:00+00', 'WAITING'),
       ((SELECT p.id FROM patient p WHERE p.public_ref = 'P-6C0PP'),
        (SELECT f.id FROM facility f WHERE f.name = 'Downtown General Hospital'),
        (SELECT s.id FROM specialty s WHERE s.name = 'Radiology'),
        1, '2026-01-20 08:30:00+00', 'WAITING');

-- ---------- Extra appointment slots ----------
-- Kabuki General Medicine: create a slot that will become FULL, and one that remains AVAILABLE due to CANCELLED booking
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity)
VALUES ((SELECT f.id FROM facility f WHERE f.name = 'Kabuki Street Clinic'),
        (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
        '2026-01-20 10:00:00+00', '2026-01-20 10:20:00+00', 1),
       ((SELECT f.id FROM facility f WHERE f.name = 'Kabuki Street Clinic'),
        (SELECT s.id FROM specialty s WHERE s.name = 'General Medicine'),
        '2026-01-20 10:20:00+00', '2026-01-20 10:40:00+00', 1);

-- Downtown Cardiology: capacity 2, only one active booking -> AVAILABLE
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity)
VALUES ((SELECT f.id FROM facility f WHERE f.name = 'Downtown General Hospital'),
        (SELECT s.id FROM specialty s WHERE s.name = 'Cardiology'),
        '2026-01-20 10:30:00+00', '2026-01-20 11:00:00+00', 2);

-- Downtown Radiology: create a slot that becomes FULL with 2 active bookings
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity)
VALUES ((SELECT f.id FROM facility f WHERE f.name = 'Downtown General Hospital'),
        (SELECT s.id FROM specialty s WHERE s.name = 'Radiology'),
        '2026-01-20 12:00:00+00', '2026-01-20 12:30:00+00', 2);

-- ---------- Bookings for those slots ----------
-- Kabuki GM 10:00 slot capacity=1 -> FULL (CONFIRMED)
INSERT INTO booking(triage_case_id, appointment_slot_id, staff_id, status, created_at)
VALUES ((SELECT tc.id
         FROM triage_case tc
                  JOIN patient p ON p.id = tc.patient_id
         WHERE p.public_ref = 'P-7K9L2'),
        (SELECT aslt.id
         FROM appointment_slot aslt
                  JOIN facility f ON f.id = aslt.facility_id
                  JOIN specialty s ON s.id = aslt.specialty_id
         WHERE f.name = 'Kabuki Street Clinic'
           AND s.name = 'General Medicine'
           AND aslt.starts_at = '2026-01-20 10:00:00+00'),
        (SELECT st.id FROM staff st WHERE st.handle = 'doc_kabuki_01'),
        'CONFIRMED',
        '2026-01-20 09:10:00+00');

-- Kabuki GM 10:20 slot capacity=1 -> AVAILABLE because booking is CANCELLED (should not count as active)
INSERT INTO booking(triage_case_id, appointment_slot_id, staff_id, status, created_at)
VALUES ((SELECT tc.id
         FROM triage_case tc
                  JOIN patient p ON p.id = tc.patient_id
         WHERE p.public_ref = 'P-0M3X8'),
        (SELECT aslt.id
         FROM appointment_slot aslt
                  JOIN facility f ON f.id = aslt.facility_id
                  JOIN specialty s ON s.id = aslt.specialty_id
         WHERE f.name = 'Kabuki Street Clinic'
           AND s.name = 'General Medicine'
           AND aslt.starts_at = '2026-01-20 10:20:00+00'),
        (SELECT st.id FROM staff st WHERE st.handle = 'doc_kabuki_01'),
        'CANCELLED',
        '2026-01-20 09:12:00+00');

-- Downtown Cardiology 10:30 slot capacity=2 -> AVAILABLE (1 RESERVED booking)
INSERT INTO booking(triage_case_id, appointment_slot_id, staff_id, status, created_at)
VALUES ((SELECT tc.id
         FROM triage_case tc
                  JOIN patient p ON p.id = tc.patient_id
         WHERE p.public_ref = 'P-4J2QA'),
        (SELECT aslt.id
         FROM appointment_slot aslt
                  JOIN facility f ON f.id = aslt.facility_id
                  JOIN specialty s ON s.id = aslt.specialty_id
         WHERE f.name = 'Downtown General Hospital'
           AND s.name = 'Cardiology'
           AND aslt.starts_at = '2026-01-20 10:30:00+00'),
        (SELECT st.id FROM staff st WHERE st.handle = 'cardio_dt_01'),
        'RESERVED',
        '2026-01-20 09:20:00+00');

-- Downtown Radiology 12:00 slot capacity=2 -> FULL (2 active bookings: RESERVED + CONFIRMED)
INSERT INTO booking(triage_case_id, appointment_slot_id, staff_id, status, created_at)
VALUES ((SELECT tc.id
         FROM triage_case tc
                  JOIN patient p ON p.id = tc.patient_id
         WHERE p.public_ref = 'P-9T1RN'),
        (SELECT aslt.id
         FROM appointment_slot aslt
                  JOIN facility f ON f.id = aslt.facility_id
                  JOIN specialty s ON s.id = aslt.specialty_id
         WHERE f.name = 'Downtown General Hospital'
           AND s.name = 'Radiology'
           AND aslt.starts_at = '2026-01-20 12:00:00+00'),
        (SELECT st.id FROM staff st WHERE st.handle = 'radio_dt_01'),
        'RESERVED',
        '2026-01-20 09:25:00+00'),
       ((SELECT tc.id
         FROM triage_case tc
                  JOIN patient p ON p.id = tc.patient_id
         WHERE p.public_ref = 'P-6C0PP'),
        (SELECT aslt.id
         FROM appointment_slot aslt
                  JOIN facility f ON f.id = aslt.facility_id
                  JOIN specialty s ON s.id = aslt.specialty_id
         WHERE f.name = 'Downtown General Hospital'
           AND s.name = 'Radiology'
           AND aslt.starts_at = '2026-01-20 12:00:00+00'),
        (SELECT st.id FROM staff st WHERE st.handle = 'radio_dt_01'),
        'CONFIRMED',
        '2026-01-20 09:26:00+00');

