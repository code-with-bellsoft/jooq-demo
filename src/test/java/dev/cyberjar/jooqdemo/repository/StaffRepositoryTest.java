package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.StaffDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
class StaffRepositoryTest {

    @Autowired
    private StaffRepository staffRepository;

    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:16-alpine"
    );

    @Test
    void shouldRetrieveDataWithImplicitJoin() {
        List<StaffDto> staff = staffRepository.findStaffBySpecialtyIdImplicitJoin(2L);

        assertThat(staff).isNotEmpty();
        assertThat(staff.size()).isEqualTo(1);

        StaffDto staffDto = staff.getFirst();
        assertThat(staffDto.facilityName()).isEqualTo("Downtown General Hospital");
        assertThat(staffDto.specialtyName()).isEqualTo("Cardiology");
        assertThat(staffDto.districtName()).isEqualTo("Downtown");

    }

}
