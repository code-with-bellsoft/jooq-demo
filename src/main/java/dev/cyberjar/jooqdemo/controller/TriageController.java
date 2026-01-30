package dev.cyberjar.jooqdemo.controller;

import dev.cyberjar.jooqdemo.dto.TriageCaseDetailsDto;
import dev.cyberjar.jooqdemo.service.TriageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/triage")
public class TriageController {


    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<TriageCaseDetailsDto> getTriageCaseById(@PathVariable Long id) {
        TriageCaseDetailsDto triageDto = triageService.findTriageCase(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Triage case not found"));
        return ResponseEntity.ok(triageDto);

    }

}
