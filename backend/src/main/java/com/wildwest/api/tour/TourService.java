package com.wildwest.api.tour;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TourService {

    private final TourRepository repository;

    public TourService(TourRepository repository) {
        this.repository = repository;
    }

    public List<TourSummaryDto> search(TourFilter filter) {
        Specification<Tour> spec = buildSpec(filter);
        Sort sort = resolveSort(filter.sort());
        return repository.findAll(spec, sort).stream()
                .map(TourSummaryDto::from)
                .toList();
    }

    private static Specification<Tour> buildSpec(TourFilter f) {
        return Specification.allOf(
                TourSpecifications.destinationIs(f.destination()),
                TourSpecifications.hasStyle(f.style()),
                TourSpecifications.priceAtLeast(f.minPrice()),
                TourSpecifications.priceAtMost(f.maxPrice()),
                TourSpecifications.durationAtLeast(f.minDays()),
                TourSpecifications.durationAtMost(f.maxDays()),
                TourSpecifications.matchesText(f.q()),
                TourSpecifications.isFeatured(f.featured())
        );
    }

    private static Sort resolveSort(String key) {
        return switch (key == null ? "" : key) {
            case "price-asc"     -> Sort.by("priceCents").ascending();
            case "price-desc"    -> Sort.by("priceCents").descending();
            case "rating"        -> Sort.by("rating").descending();
            case "duration-asc"  -> Sort.by("durationDays").ascending();
            case "duration-desc" -> Sort.by("durationDays").descending();
            default              -> Sort.by("displayOrder").ascending();
        };
    }
}