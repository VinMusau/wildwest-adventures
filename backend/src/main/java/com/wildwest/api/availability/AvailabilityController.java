package com.wildwest.api.availability;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tours/{slug}/availability")
public class AvailabilityController {

    private final AvailabilityRepository repository;

    public AvailabilityController(AvailabilityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AvailabilityDto> list(
            @PathVariable String slug,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start.plusMonths(6);
        return repository.findOpenForTourBetween(slug, start, end).stream()
                .map(AvailabilityDto::from)
                .toList();
    }
}
