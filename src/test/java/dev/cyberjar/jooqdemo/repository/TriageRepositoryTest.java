package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.BookingDto;
import dev.cyberjar.jooqdemo.dto.LabOrderDto;
import dev.cyberjar.jooqdemo.dto.LabResultDto;
import dev.cyberjar.jooqdemo.dto.TriageCaseDetailsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
class TriageRepositoryTest {

    @Autowired
    TriageRepository triageRepository;

    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:16-alpine"
    );

    @Test
    void shouldReturnBookingsMultiset() {
        long triageCaseId = 2L;

        Optional<TriageCaseDetailsDto> opt = triageRepository.findTriageCase(triageCaseId);

        assertThat(opt).isPresent();
        TriageCaseDetailsDto dto = opt.get();

        assertThat(dto.bookings()).isNotNull();
        assertThat(dto.bookings().size()).isEqualTo(1);

        BookingDto b = dto.bookings().getFirst();
        assertThat(b.status()).isEqualTo("RESERVED");
        assertThat(b.facilityName()).isEqualTo("Kabuki Street Clinic");

    }

    @Test
    void shouldReturnLabOrdersWithResult() {
        long triageCaseId = 5L;

        Optional<TriageCaseDetailsDto> opt = triageRepository.findTriageCase(triageCaseId);

        assertThat(opt).isPresent();
        TriageCaseDetailsDto dto = opt.get();


        assertThat(dto.labOrders()).isNotNull();
        assertThat(dto.labOrders().size()).isEqualTo(1);

        LabOrderDto lo = dto.labOrders().getFirst();
        assertThat(lo.testCode()).isEqualTo("XRAY-CHEST");

        assertThat(lo.results()).isNotNull();
        assertThat(lo.results().size()).isEqualTo(1);

        LabResultDto r = lo.results().getFirst();
        assertThat(r.status()).isEqualTo("READY");

    }


}