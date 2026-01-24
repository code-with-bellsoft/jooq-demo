package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.SlotDto;
import dev.cyberjar.jooqdemo.dto.SlotFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
class AppointmentSlotRepositoryTest {


    @Autowired
    AppointmentSlotRepository slotRepository;

    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:16-alpine"
    );

    @Test
    void findSlotById() {
        SlotDto slot = slotRepository.findSlotById(1L).orElseThrow();

        assertThat(slot.facilityId()).isEqualTo(1L);
        assertThat(slot.specialtyName()).isEqualTo("General Medicine");
    }


    @Test
    void findSlotsWithFacilityFilter() {
        List<SlotDto> slots = slotRepository.findSlots(new SlotFilter(1L, 1L, null));

        assertThat(slots).isNotEmpty();
        assertThat(slots).allSatisfy(s -> assertThat(s.facilityId()).isEqualTo(1L));
    }

    @Test
    void findSlotsWithDistrictFilter() {

        List<SlotDto> neonSlots = slotRepository.findSlots(new SlotFilter(null, 1L, 4L));

        assertThat(neonSlots).isNotEmpty();
        assertThat(neonSlots)
                .allSatisfy(s -> assertThat(s.facilityName()).isEqualTo("NeonCare Mobile Pod #7"));

    }


    @Test
    void findSlotsWithOnlySpecialtyFilter() {

        List<SlotDto> slots = slotRepository.findSlots(new SlotFilter(null, 1L, null));

        assertThat(slots).isNotEmpty();

        Set<String> facilityNames = slots.stream().map(SlotDto::facilityName).collect(Collectors.toSet());

        assertThat(facilityNames).contains(
                "Kabuki Street Clinic",
                "NeonCare Mobile Pod #7",
                "Harbor Community Clinic",
                "Downtown General Hospital"
        );
    }


    @Test
    void shouldNotShowFullyBookedSlots() {

        // Fully booked from migrations:
        // - Kabuki GM 09:00 (capacity 2) FULL (2 active bookings)
        OffsetDateTime full1 = OffsetDateTime.parse("2026-01-20T09:00:00Z");

        List<SlotDto> slots = slotRepository.findSlots(new SlotFilter(null, 1L, null));

        assertThat(slots).noneSatisfy(s -> assertThat(s.startsAt()).isEqualTo(full1));

        // Confirm at least one known available slot exists (V4)
        assertThat(slots).anySatisfy(s ->
                assertThat(s.startsAt()).isEqualTo(OffsetDateTime.parse("2026-01-20T13:00:00Z"))
        );

        // Confirm a CANCELLED booking does not block availability (Kabuki GM 10:20 was cancelled in V3)
        assertThat(slots).anySatisfy(s ->
                assertThat(s.startsAt()).isEqualTo(OffsetDateTime.parse("2026-01-20T10:20:00Z"))
        );

    }


}