package dev.cyberjar.jooqdemo.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record SlotDto(
        long id,
        @NotNull @Positive long facilityId,
        String facilityName,
        @NotNull @Positive long specialtyId,
        String specialtyName,
        @NotNull @Positive @Future OffsetDateTime startsAt,
        @NotNull @Positive @Future OffsetDateTime endsAt,
        @NotNull @Positive int capacity
) {}
