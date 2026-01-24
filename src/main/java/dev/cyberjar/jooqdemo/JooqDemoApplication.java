package dev.cyberjar.jooqdemo;

import dev.cyberjar.jooqdemo.dto.SlotDto;
import dev.cyberjar.jooqdemo.dto.SlotFilter;
import dev.cyberjar.jooqdemo.service.AppointmentSlotService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class JooqDemoApplication implements CommandLineRunner {

    private final AppointmentSlotService slotService;
    private static final String slotPrint = "Slot %d%nFacility: %s (id=%d)%nSpecialty: %s (id=%d)%nTime: %s -> %s%nCapacity: %d%n";


    public JooqDemoApplication(AppointmentSlotService slotService) {
        this.slotService = slotService;
    }

    public static void main(String[] args) {
        SpringApplication.run(JooqDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        SlotFilter filterWithFacility = new SlotFilter(1L, 1L, null);
        SlotFilter filterWithDistrict = new SlotFilter(null, 1L, 2L);
        SlotFilter filterWithSpecialty = new SlotFilter(null, 2L, null);


        List<SlotDto> slotsWithFacility = slotService.findSlots(filterWithFacility);
        List<SlotDto> slotsWithDistrict = slotService.findSlots(filterWithDistrict);
        List<SlotDto> slotsWithSpecialty = slotService.findSlots(filterWithSpecialty);

        System.out.println("\n=== FIND SLOTS WITH FACILITY ===");

        for (SlotDto slot : slotsWithFacility) {
            System.out.printf(
                    slotPrint,
                    slot.id(),
                    slot.facilityName(), slot.facilityId(),
                    slot.specialtyName(), slot.specialtyId(),
                    slot.startsAt(), slot.endsAt(),
                    slot.capacity()
            );
        }

        System.out.println("\n=== FIND SLOTS WITH DISTRICT ===");

        for (SlotDto slot : slotsWithDistrict) {
            System.out.printf(
                    slotPrint,
                    slot.id(),
                    slot.facilityName(), slot.facilityId(),
                    slot.specialtyName(), slot.specialtyId(),
                    slot.startsAt(), slot.endsAt(),
                    slot.capacity()
            );
        }


        System.out.println("\n=== FIND ANY SLOTS WITH SPECIALTY ===");

        for (SlotDto slot : slotsWithSpecialty) {
            System.out.printf(
                    slotPrint,
                    slot.id(),
                    slot.facilityName(), slot.facilityId(),
                    slot.specialtyName(), slot.specialtyId(),
                    slot.startsAt(), slot.endsAt(),
                    slot.capacity()
            );
        }


    }
}
