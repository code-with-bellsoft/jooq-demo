package dev.cyberjar.jooqdemo.dto;

import java.time.OffsetDateTime;

public record SlotFilter(Long facilityId,
                         Long specialtyId,
                         OffsetDateTime from,
                         OffsetDateTime to,
                         Long districtId
) { }
