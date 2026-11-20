package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminTourDtos.AvailabilityWriteRequest;
import com.wildwest.api.admin.AdminTourDtos.TourAdminDetail;
import com.wildwest.api.admin.AdminTourDtos.TourAdminSummary;
import com.wildwest.api.admin.AdminTourDtos.TourWriteRequest;
import com.wildwest.api.availability.AvailabilityDto;
import com.wildwest.api.image.CloudinaryService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/tours")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTourController {

    private final AdminTourService service;
    private final CloudinaryService cloudinaryService;

    public AdminTourController(AdminTourService service, CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
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
    public TourAdminDetail update(@PathVariable String slug, @Valid @RequestBody TourWriteRequest request) {
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
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
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
    public List<AvailabilityDto> listAvailability(@PathVariable String slug) {
        return service.listAvailability(slug);
    }

    @PostMapping("/{slug}/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityDto addAvailability(
            @PathVariable String slug, @Valid @RequestBody AvailabilityWriteRequest request) {
        return service.addAvailability(slug, request);
    }

    @DeleteMapping("/availability/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(@PathVariable UUID id) {
        service.deleteAvailability(id);
    }
}
