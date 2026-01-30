package dev.cyberjar.jooqdemo.controller;

import dev.cyberjar.jooqdemo.dto.SlotDto;
import dev.cyberjar.jooqdemo.filter.SlotFilter;
import dev.cyberjar.jooqdemo.service.AppointmentSlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class AppointmentSlotController {

    private final AppointmentSlotService appointmentSlotService;

    public AppointmentSlotController(AppointmentSlotService appointmentSlotService) {
        this.appointmentSlotService = appointmentSlotService;
    }

    @GetMapping
    public ResponseEntity<List<SlotDto>> getAllSlots(@RequestBody SlotFilter filter) {
        return ResponseEntity.ok(appointmentSlotService.findSlots(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SlotDto> getSlotById(@PathVariable Long id) {
        SlotDto slot = appointmentSlotService.findSlotById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        return ResponseEntity.ok(slot);

    }


    @PostMapping("/register")
    public ResponseEntity<Long> createSlot(@RequestBody SlotDto slotDto) {

        Long userId = appointmentSlotService.createSlot(slotDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSlot(@PathVariable Long id, @RequestBody int newCapacity) {
        appointmentSlotService.updateSlot(id, newCapacity);
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        appointmentSlotService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }

}
