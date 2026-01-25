package dev.cyberjar.jooqdemo.service;

import dev.cyberjar.jooqdemo.dto.SlotDto;
import dev.cyberjar.jooqdemo.filter.SlotFilter;
import dev.cyberjar.jooqdemo.repository.AppointmentSlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppointmentSlotService {

    private final AppointmentSlotRepository repository;

    public AppointmentSlotService(AppointmentSlotRepository appointmentSlotRepository) {
        this.repository = appointmentSlotRepository;
    }

    public List<SlotDto> findSlots(SlotFilter filter) {
        return repository.findSlots(filter);
    }

    public Optional<SlotDto> findSlotById(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return repository.findSlotById(id);
    }

    public Long createSlot(SlotDto slot) {

        Objects.requireNonNull(slot, "slot must not be null");

        if (!slot.endsAt().isAfter(slot.startsAt())) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }

        return repository.createSlot(slot);
    }

    public int updateSlot(Long slotId, int newCapacity) {

        if (newCapacity <= 0) throw new IllegalArgumentException("newCapacity must be > 0");
        return repository.updateSlot(slotId, newCapacity);
    }

    public int deleteSlot(Long slotId) {
        return repository.deleteSlot(slotId);
    }


}
