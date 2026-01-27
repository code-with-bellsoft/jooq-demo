package dev.cyberjar.jooqdemo.dto;

public record FacilityDto(
        Long facilityId,
        String facilityName,
        int totalTravelMinutes
) { }
