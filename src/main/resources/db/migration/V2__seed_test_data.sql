-- Seed data for demos

INSERT INTO district(name) VALUES
                               ('Kabuki'),
                               ('Harbor'),
                               ('Downtown'),
                               ('Neon Heights');

INSERT INTO facility(name, type, district_id) VALUES
                                                  ('Kabuki Street Clinic', 'CLINIC', (SELECT id FROM district WHERE name='Kabuki')),
                                                  ('Harbor Diagnostics Lab', 'LAB', (SELECT id FROM district WHERE name='Harbor')),
                                                  ('Downtown General Hospital', 'HOSPITAL', (SELECT id FROM district WHERE name='Downtown')),
                                                  ('NeonCare Mobile Pod #7', 'MOBILE', (SELECT id FROM district WHERE name='Neon Heights'));

-- Transfer graph (directed)
INSERT INTO facility_link(from_facility_id, to_facility_id, travel_minutes) VALUES
                                                                                ((SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                 (SELECT id FROM facility WHERE name='Downtown General Hospital'), 18),
                                                                                ((SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                 (SELECT id FROM facility WHERE name='Harbor Diagnostics Lab'), 14),
                                                                                ((SELECT id FROM facility WHERE name='Harbor Diagnostics Lab'),
                                                                                 (SELECT id FROM facility WHERE name='Downtown General Hospital'), 16),
                                                                                ((SELECT id FROM facility WHERE name='NeonCare Mobile Pod #7'),
                                                                                 (SELECT id FROM facility WHERE name='Kabuki Street Clinic'), 11),
                                                                                ((SELECT id FROM facility WHERE name='NeonCare Mobile Pod #7'),
                                                                                 (SELECT id FROM facility WHERE name='Downtown General Hospital'), 22);

INSERT INTO specialty(name) VALUES
                                ('General Medicine'),
                                ('Cardiology'),
                                ('Radiology'),
                                ('Pathology');

-- Staff: use handles, not names
INSERT INTO staff(facility_id, specialty_id, handle, active) VALUES
                                                                 ((SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                  (SELECT id FROM specialty WHERE name='General Medicine'), 'doc_kabuki_01', TRUE),

                                                                 ((SELECT id FROM facility WHERE name='Downtown General Hospital'),
                                                                  (SELECT id FROM specialty WHERE name='Cardiology'), 'cardio_dt_01', TRUE),
                                                                 ((SELECT id FROM facility WHERE name='Downtown General Hospital'),
                                                                  (SELECT id FROM specialty WHERE name='Radiology'), 'radio_dt_01', TRUE),

                                                                 ((SELECT id FROM facility WHERE name='Harbor Diagnostics Lab'),
                                                                  (SELECT id FROM specialty WHERE name='Pathology'), 'path_harbor_01', TRUE),

                                                                 ((SELECT id FROM facility WHERE name='NeonCare Mobile Pod #7'),
                                                                  (SELECT id FROM specialty WHERE name='General Medicine'), 'med_pod7_01', TRUE);

-- Patients (synthetic IDs)
INSERT INTO patient(public_ref, created_at) VALUES
                                                ('P-83K2X', '2026-01-19 09:10:00+00'),
                                                ('P-19Q7M', '2026-01-19 12:40:00+00'),
                                                ('P-5ZZ1A', '2026-01-20 07:05:00+00'),
                                                ('P-4N0VQ', '2026-01-20 07:20:00+00'),
                                                ('P-2A7TT', '2026-01-20 07:40:00+00');

-- Triage cases: enough variety for queue ranking + “unscheduled high severity”
INSERT INTO triage_case(patient_id, intake_facility_id, required_specialty_id, severity, created_at, status) VALUES
                                                                                                                 ((SELECT id FROM patient WHERE public_ref='P-83K2X'),
                                                                                                                  (SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                                                  (SELECT id FROM specialty WHERE name='General Medicine'),
                                                                                                                  2, '2026-01-20 06:30:00+00', 'WAITING'),

                                                                                                                 ((SELECT id FROM patient WHERE public_ref='P-19Q7M'),
                                                                                                                  (SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                                                  (SELECT id FROM specialty WHERE name='General Medicine'),
                                                                                                                  4, '2026-01-20 06:50:00+00', 'WAITING'),

                                                                                                                 ((SELECT id FROM patient WHERE public_ref='P-5ZZ1A'),
                                                                                                                  (SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                                                  (SELECT id FROM specialty WHERE name='General Medicine'),
                                                                                                                  5, '2026-01-20 07:10:00+00', 'WAITING'),

                                                                                                                 ((SELECT id FROM patient WHERE public_ref='P-4N0VQ'),
                                                                                                                  (SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                                                  (SELECT id FROM specialty WHERE name='Cardiology'),
                                                                                                                  3, '2026-01-20 07:15:00+00', 'REFERRED'),

                                                                                                                 ((SELECT id FROM patient WHERE public_ref='P-2A7TT'),
                                                                                                                  (SELECT id FROM facility WHERE name='Downtown General Hospital'),
                                                                                                                  (SELECT id FROM specialty WHERE name='Radiology'),
                                                                                                                  3, '2026-01-20 07:25:00+00', 'IN_TREATMENT');

-- Appointment slots (some will be filled via bookings)
INSERT INTO appointment_slot(facility_id, specialty_id, starts_at, ends_at, capacity) VALUES
                                                                                          ((SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                           (SELECT id FROM specialty WHERE name='General Medicine'),
                                                                                           '2026-01-20 09:00:00+00', '2026-01-20 09:20:00+00', 2),

                                                                                          ((SELECT id FROM facility WHERE name='Kabuki Street Clinic'),
                                                                                           (SELECT id FROM specialty WHERE name='General Medicine'),
                                                                                           '2026-01-20 09:20:00+00', '2026-01-20 09:40:00+00', 1),

                                                                                          ((SELECT id FROM facility WHERE name='Downtown General Hospital'),
                                                                                           (SELECT id FROM specialty WHERE name='Cardiology'),
                                                                                           '2026-01-20 10:00:00+00', '2026-01-20 10:30:00+00', 1),

                                                                                          ((SELECT id FROM facility WHERE name='Downtown General Hospital'),
                                                                                           (SELECT id FROM specialty WHERE name='Radiology'),
                                                                                           '2026-01-20 11:00:00+00', '2026-01-20 11:30:00+00', 2);

-- Bookings: create “scheduled vs unscheduled” situations
INSERT INTO booking(triage_case_id, appointment_slot_id, staff_id, status, created_at) VALUES
                                                                                           -- One waiting case gets a reserved slot
                                                                                           (
                                                                                               (SELECT tc.id
                                                                                                FROM triage_case tc
                                                                                                         JOIN patient p ON p.id = tc.patient_id
                                                                                                WHERE p.public_ref = 'P-19Q7M'),
                                                                                               (SELECT s.id
                                                                                                FROM appointment_slot s
                                                                                                         JOIN facility f ON f.id = s.facility_id
                                                                                                         JOIN specialty sp ON sp.id = s.specialty_id
                                                                                                WHERE f.name = 'Kabuki Street Clinic'
                                                                                                  AND sp.name = 'General Medicine'
                                                                                                  AND s.starts_at = '2026-01-20 09:00:00+00'),
                                                                                               (SELECT st.id
                                                                                                FROM staff st
                                                                                                WHERE st.handle = 'doc_kabuki_01'),
                                                                                               'RESERVED',
                                                                                               '2026-01-20 07:00:00+00'
                                                                                           ),

                                                                                           -- Fill up a slot to test capacity logic later
                                                                                           (
                                                                                               (SELECT tc.id
                                                                                                FROM triage_case tc
                                                                                                         JOIN patient p ON p.id = tc.patient_id
                                                                                                WHERE p.public_ref = 'P-83K2X'),
                                                                                               (SELECT s.id
                                                                                                FROM appointment_slot s
                                                                                                         JOIN facility f ON f.id = s.facility_id
                                                                                                         JOIN specialty sp ON sp.id = s.specialty_id
                                                                                                WHERE f.name = 'Kabuki Street Clinic'
                                                                                                  AND sp.name = 'General Medicine'
                                                                                                  AND s.starts_at = '2026-01-20 09:00:00+00'),
                                                                                               (SELECT st.id
                                                                                                FROM staff st
                                                                                                WHERE st.handle = 'doc_kabuki_01'),
                                                                                               'CONFIRMED',
                                                                                               '2026-01-20 07:02:00+00'
                                                                                           );


-- Lab orders + results: create “missing result” gap
INSERT INTO lab_order(triage_case_id, lab_facility_id, test_code, ordered_at) VALUES
                                                                                  (
                                                                                      (SELECT tc.id
                                                                                       FROM triage_case tc
                                                                                                JOIN patient p ON p.id = tc.patient_id
                                                                                       WHERE p.public_ref = 'P-4N0VQ'),
                                                                                      (SELECT f.id
                                                                                       FROM facility f
                                                                                       WHERE f.name = 'Harbor Diagnostics Lab'),
                                                                                      'CBC',
                                                                                      '2026-01-20 07:18:00+00'
                                                                                  ),
                                                                                  (
                                                                                      (SELECT tc.id
                                                                                       FROM triage_case tc
                                                                                                JOIN patient p ON p.id = tc.patient_id
                                                                                       WHERE p.public_ref = 'P-2A7TT'),
                                                                                      (SELECT f.id
                                                                                       FROM facility f
                                                                                       WHERE f.name = 'Harbor Diagnostics Lab'),
                                                                                      'XRAY-CHEST',
                                                                                      '2026-01-20 07:30:00+00'
                                                                                  );

-- Only one result published: the other is intentionally missing (anti-join demo)
INSERT INTO lab_result(lab_order_id, result_status, published_at) VALUES
    (
        (SELECT lo.id
         FROM lab_order lo
                  JOIN triage_case tc ON tc.id = lo.triage_case_id
                  JOIN patient p ON p.id = tc.patient_id
         WHERE p.public_ref = 'P-2A7TT'
           AND lo.test_code = 'XRAY-CHEST'),
        'READY',
        '2026-01-20 08:05:00+00'
    );
