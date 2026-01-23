package dev.cyberjar.jooqdemo.dto;

import java.time.OffsetDateTime;

public record SlotDto(
        long id,
        long facilityId,
        String facilityName,
        long specialtyId,
        String specialtyName,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        int capacity
) {}
