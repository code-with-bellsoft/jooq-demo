package dev.cyberjar.jooqdemo.dto;

import java.time.OffsetDateTime;

public record LabResultDto(
        String status,
        OffsetDateTime publishedAt
) { }
