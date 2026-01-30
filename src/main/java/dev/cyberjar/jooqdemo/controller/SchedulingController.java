package dev.cyberjar.jooqdemo.controller;

import dev.cyberjar.jooqdemo.dto.SlotSuggestionDto;
import dev.cyberjar.jooqdemo.service.SchedulingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/{triageCaseId}")
    public ResponseEntity<List<SlotSuggestionDto>> getSuggestedSlotsByTriageCaseId(@PathVariable Long triageCaseId) {
        return ResponseEntity.ok(schedulingService.suggestSlotsForTriageCase(triageCaseId));
    }

}
