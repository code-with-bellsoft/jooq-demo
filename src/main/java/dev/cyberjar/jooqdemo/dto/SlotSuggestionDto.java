package dev.cyberjar.jooqdemo.dto;

import java.time.OffsetDateTime;

public record SlotSuggestionDto (
        Long slotId,
        OffsetDateTime slotStartsAt,
        OffsetDateTime slotEndsAt,
        String facilityName,
        String districtName,
        String specialtyName,
        int remainingCapacity
) { }