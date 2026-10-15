package com.wildwest.api.availability;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tours/{slug}/availability")
public class AvailabilityController {

    private final AvailabilityRepository repository;

    public AvailabilityController(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public record AvailabilityDto(
            UUID id,
            LocalDate date,
            int capacity,
            int booked,
            int remaining,
            int priceCents,
            String currency,
            String status
    ) {
        static AvailabilityDto from(Availability a) {
            return new AvailabilityDto(
                    a.getId(),
                    a.getDepartureDate(),
                    a.getCapacity(),
                    a.getBooked(),
                    a.remaining(),
                    a.effectivePriceCents(),
                    a.getCurrency(),
                    a.getStatus()
            );
        }
    }

    @GetMapping
    public List<AvailabilityDto> list(
            @PathVariable String slug,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start.plusMonths(6);
        return repository.findOpenForTourBetween(slug, start, end)
                .stream()
                .map(AvailabilityDto::from)
                .toList();
    }
}