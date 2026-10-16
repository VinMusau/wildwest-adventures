package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminBookingDtos.BookingAdminDetail;
import com.wildwest.api.admin.AdminBookingDtos.BookingAdminSummary;
import com.wildwest.api.admin.AdminBookingDtos.CancelRequest;
import com.wildwest.api.admin.AdminBookingDtos.RefundRequest;
import com.wildwest.api.booking.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final AdminBookingService service;

    public AdminBookingController(AdminBookingService service) {
        this.service = service;
    }

    @GetMapping
    public Page<BookingAdminSummary> list(
            @RequestParam(required = false) Booking.Status status,
            @RequestParam(required = false) String tour,
            @RequestParam(required = false) String q,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("createdAt").descending()
        );
        return service.list(status, tour, q, from, to, pageable);
    }

    @GetMapping("/{reference}")
    public BookingAdminDetail get(@PathVariable String reference) {
        return service.get(reference);
    }

    @PostMapping("/{reference}/cancel")
    public BookingAdminDetail cancel(
            @PathVariable String reference,
            @RequestBody(required = false) CancelRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        return service.cancel(reference, reason);
    }

    @PostMapping("/{reference}/confirm")
    public BookingAdminDetail confirm(@PathVariable String reference) {
        return service.confirm(reference);
    }

    @PostMapping("/{reference}/refund")
    public BookingAdminDetail refund(
            @PathVariable String reference,
            @RequestBody(required = false) RefundRequest request
    ) {
        Integer amount = request != null ? request.amountCents() : null;
        return service.refund(reference, amount);
    }
}