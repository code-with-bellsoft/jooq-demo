package dev.cyberjar.jooqdemo.service;

import dev.cyberjar.jooqdemo.dto.SlotDto;
import dev.cyberjar.jooqdemo.repository.AppointmentSlotRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppointmentSlotService {

    private final AppointmentSlotRepository repository;

    public AppointmentSlotService(AppointmentSlotRepository appointmentSlotRepository) {
        this.repository = appointmentSlotRepository;
    }

    public Optional<SlotDto> findSlotById(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return repository.findSlotById(id);
    }

}
