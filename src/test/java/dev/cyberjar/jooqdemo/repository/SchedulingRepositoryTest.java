package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.SlotSuggestionDto;
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
class SchedulingRepositoryTest {

    @Autowired
    private SchedulingRepository schedulingRepository;

    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:16-alpine"
    );

    @Test
    void shouldReturnSlotsWithCalculatedCapacity() {
        List<SlotSuggestionDto> suggestions = schedulingRepository.suggestSlots(2L);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.getFirst().remainingCapacity()).isEqualTo(1);

    }
}
