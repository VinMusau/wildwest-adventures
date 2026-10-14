package com.wildwest.api.destination;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/destinations")
public class DestinationController {

    private final DestinationRepository repository;

    public DestinationController(DestinationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DestinationDto> list() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(DestinationDto::from)
                .toList();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<DestinationDto> bySlug(@PathVariable String slug) {
        return repository.findBySlug(slug)
                .map(DestinationDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}