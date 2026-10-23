package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminTourDtos.TourAdminDetail;
import com.wildwest.api.admin.AdminTourDtos.TourAdminSummary;
import com.wildwest.api.admin.AdminTourDtos.TourWriteRequest;
import com.wildwest.api.image.CloudinaryService;
import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tours")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTourController {

    private final AdminTourService service;
    private final CloudinaryService cloudinaryService;
    private final AvailabilityRepository availabilityRepository;

    public AdminTourController(AdminTourService service, CloudinaryService cloudinaryService, AvailabilityRepository availabilityRepository) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
        this.availabilityRepository = availabilityRepository;
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

    @PostMapping("/upload-image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        // Basic Validation
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "File too large");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image files are allowed");
        }

        try {
            String imageUrl = cloudinaryService.uploadImage(file, "wildwest_adventures/tours");
            return Map.of("url", imageUrl);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }

    @GetMapping("/{slug}/availability")
    public List<Availability> listAvailability(@PathVariable String slug) {
        return availabilityRepository.findByTourSlugOrderByDepartureDateAsc(slug);
    }

    @PostMapping("/{slug}/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public Availability addAvailability(
            @PathVariable String slug,
            @RequestBody Map<String, Object> body
    ) {
        LocalDate date = LocalDate.parse((String) body.get("date"));
        Integer capacity = (Integer) body.get("capacity");

        // In a real app, use a DTO instead of Map, but this is faster for now
        return service.addAvailability(slug, date, capacity);
    }

    @DeleteMapping("/availability/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(@PathVariable UUID id) {
        service.deleteAvailability(id);
    }
}