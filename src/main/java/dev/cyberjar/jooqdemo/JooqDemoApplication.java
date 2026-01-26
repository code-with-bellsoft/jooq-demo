package dev.cyberjar.jooqdemo;

import dev.cyberjar.jooqdemo.dto.*;
import dev.cyberjar.jooqdemo.service.SchedulingService;
import dev.cyberjar.jooqdemo.service.TriageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class JooqDemoApplication implements CommandLineRunner {

    private final TriageService triageService;
    private final SchedulingService schedulingService;


    public JooqDemoApplication(TriageService triageService, SchedulingService schedulingService) {
        this.triageService = triageService;
        this.schedulingService = schedulingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(JooqDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        //showTriageCaseDemo();
        showAppointmentSlotSuggestionDemo();


    }

    private void showAppointmentSlotSuggestionDemo() {

        Long triageCaseId = 2L;
        List<SlotSuggestionDto> suggestions = schedulingService.suggestSlotsForTriageCase(triageCaseId);


        System.out.println("\n=== SUGGESTED APPOINTMENT SLOTS FOR TRIAGE CASE ===");

        System.out.printf("%-6s  %-20s  %-20s  %-26s  %-12s  %-6s  %-9s%n",
                "ID", "START", "END", "FACILITY", "DISTRICT", "SPECIALTY", "REMAIN_CAP");

        for (SlotSuggestionDto s : suggestions) {
            System.out.printf("%-6s  %-20s  %-20s  %-26s  %-12s  %-6s  %-9s%n",
                    s.slotId(),
                    s.slotStartsAt(),
                    s.slotEndsAt(),
                    s.facilityName(),
                    s.districtName(),
                    s.specialtyName(),
                    s.remainingCapacity()
            );
        }


    }

    private void showTriageCaseDemo() {
        Long triageCaseId = 1L;

        Optional<TriageCaseDetailsDto> triageDto = triageService.findTriageCase(triageCaseId);

        System.out.println("\n=== TRIAGE CASE DETAILS ===");

        if (triageDto.isEmpty()) {
            System.out.printf("No triage case found for id=%d%n", triageCaseId);
            return;
        }

        TriageCaseDetailsDto c = triageDto.get();

        System.out.printf(
                "Case #%d | patient=%s | severity=%s | status=%s%n",
                c.caseId(),
                c.patientPublicRef(),
                c.severity(),
                c.status()
        );
        System.out.printf(
                "Intake: %s | Required specialty: %s | Created: %s%n",
                c.intakeFacilityName(),
                c.requiredSpecialtyName(),
                c.createdAt()
        );

        // Bookings
        var bookings = c.bookings() == null ? List.<BookingDto>of() : c.bookings();
        System.out.printf("%nBookings (%d)%n", bookings.size());
        if (bookings.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (BookingDto b : bookings) {
                System.out.printf(
                        "  - bookingId=%d | %s | created=%s | facility=%s | slot=%s | staff=%s%n",
                        b.bookingId(),
                        b.status(),
                        b.createdAt(),
                        b.facilityName(),
                        b.slotStartsAt(),
                        b.staffHandle() == null ? "(unassigned)" : b.staffHandle()
                );
            }
        }

        // Lab Orders + Results
        var labOrders = c.labOrders() == null ? List.<LabOrderDto>of() : c.labOrders();
        System.out.printf("%nLab orders (%d)%n", labOrders.size());
        if (labOrders.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (LabOrderDto lo : labOrders) {
                System.out.printf(
                        "  - labOrderId=%d | test=%s | lab=%s | ordered=%s%n",
                        lo.labOrderId(),
                        lo.testCode(),
                        lo.labFacilityName(),
                        lo.orderedAt()
                );

                List<LabResultDto> results = lo.results() == null ? List.of() : lo.results();
                if (results.isEmpty()) {
                    System.out.println("      results: (none)");
                } else {
                    for (LabResultDto r : results) {
                        System.out.printf(
                                "      result: %s | published=%s%n",
                                r.status(),
                                r.publishedAt()
                        );
                    }
                }
            }
        }
    }
}
