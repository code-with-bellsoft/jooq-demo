package dev.cyberjar.jooqdemo.service;

import dev.cyberjar.jooqdemo.dto.StaffDto;
import dev.cyberjar.jooqdemo.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public List<StaffDto> findStaffBySpecialtyId(Long specialtyId) {

        if (specialtyId <= 0) {
            return new ArrayList<>();
        }

        return staffRepository.findStaffBySpecialtyIdImplicitJoin(specialtyId);
    }

}
