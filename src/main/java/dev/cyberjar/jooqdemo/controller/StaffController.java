package dev.cyberjar.jooqdemo.controller;

import dev.cyberjar.jooqdemo.dto.StaffDto;
import dev.cyberjar.jooqdemo.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/{specialtyId}")
    public ResponseEntity<List<StaffDto>> getAllSlotsBySpecialtyId(@PathVariable Long specialtyId) {
        return ResponseEntity.ok(staffService.findStaffBySpecialtyId(specialtyId));
    }

}
