package dev.cyberjar.jooqdemo.filter;


import jakarta.validation.constraints.NotNull;

public record SlotFilter(Long facilityId,
                         @NotNull Long specialtyId,
                         Long districtId
) { }
