package com.wildwest.api.booking;

import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public BookingDto create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return service.create(user, request);
    }

    @GetMapping("/{reference}")
    public BookingDto byReference(
            @AuthenticationPrincipal User user,
            @PathVariable String reference
    ) {
        return service.getByReference(user, reference);
    }

    @GetMapping("/mine")
    public List<BookingDto> mine(@AuthenticationPrincipal User user) {
        return service.listMine(user);
    }
}