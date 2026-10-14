package com.wildwest.api.travelstyle;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/travel-styles")
public class TravelStyleController {

    private final TravelStyleRepository repository;

    public TravelStyleController(TravelStyleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TravelStyleDto> list() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(TravelStyleDto::from)
                .toList();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<TravelStyleDto> bySlug(@PathVariable String slug) {
        return repository.findBySlug(slug)
                .map(TravelStyleDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}