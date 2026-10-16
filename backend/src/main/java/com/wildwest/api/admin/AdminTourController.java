package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminTourDtos.TourAdminDetail;
import com.wildwest.api.admin.AdminTourDtos.TourAdminSummary;
import com.wildwest.api.admin.AdminTourDtos.TourWriteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/tours")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTourController {

    private final AdminTourService service;

    public AdminTourController(AdminTourService service) {
        this.service = service;
    }

    @GetMapping
    public List<TourAdminSummary> list() {
        return service.list();
    }

    @GetMapping("/{slug}")
    public TourAdminDetail get(@PathVariable String slug) {
        return service.get(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourAdminDetail create(@Valid @RequestBody TourWriteRequest request) {
        return service.create(request);
    }

    @PutMapping("/{slug}")
    public TourAdminDetail update(
            @PathVariable String slug,
            @Valid @RequestBody TourWriteRequest request
    ) {
        return service.update(slug, request);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug) {
        service.delete(slug);
    }

    @GetMapping("/_ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok", "message", "admin tours reachable");
    }
}