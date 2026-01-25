package dev.cyberjar.jooqdemo.dto;

public record StaffDto(Long staffId,
                       String handle,
                       boolean active,
                       String specialtyName,
                       String facilityName,
                       String facilityType,
                       String districtName
) { }
