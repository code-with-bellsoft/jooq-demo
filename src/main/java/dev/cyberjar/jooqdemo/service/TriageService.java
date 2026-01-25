package dev.cyberjar.jooqdemo.service;

import dev.cyberjar.jooqdemo.dto.TriageCaseDetailsDto;
import dev.cyberjar.jooqdemo.repository.TriageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TriageService {

    private final TriageRepository triageRepository;

    public TriageService(TriageRepository triageRepository) {
        this.triageRepository = triageRepository;
    }

    public Optional<TriageCaseDetailsDto> findTriageCase(Long triageCaseId) {
        if (triageCaseId <= 0) {
            return Optional.empty();
        }

        return triageRepository.findTriageCase(triageCaseId);
    }

}
