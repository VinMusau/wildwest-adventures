package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminDashboardDtos.*;
import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final BookingRepository bookingRepository;

    public AdminDashboardService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public DashboardResponse load() {
        Instant now = Instant.now();
        Instant thisMonthStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1)
                .atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant lastMonthStart = thisMonthStart.minus(30, ChronoUnit.DAYS);
        Instant seriesStart = LocalDate.now(ZoneOffset.UTC).minusDays(90)
                .atStartOfDay().toInstant(ZoneOffset.UTC);

        KpiSummary kpis = new KpiSummary(
                bookingRepository.countCreatedBetween(thisMonthStart, now.plusSeconds(1)),
                bookingRepository.countCreatedBetween(lastMonthStart, thisMonthStart),
                bookingRepository.sumRevenueBetween(thisMonthStart, now.plusSeconds(1)),
                bookingRepository.sumRevenueBetween(lastMonthStart, thisMonthStart),
                bookingRepository.countByStatus(Booking.Status.CONFIRMED),
                bookingRepository.countByStatus(Booking.Status.CANCELLED),
                bookingRepository.countByStatus(Booking.Status.PENDING),
                bookingRepository.countByStatus(Booking.Status.EXPIRED),
                bookingRepository.count()
        );

        List<RevenuePoint> series = bookingRepository.revenueByDay(seriesStart).stream()
                .map(p -> new RevenuePoint(p.getBucket(), p.getRevenueCents(), p.getBookingsCount()))
                .toList();

        List<TopTour> topTours = bookingRepository.findTopTours(5).stream()
                .map(p -> new TopTour(
                        p.getTourSlug(),
                        p.getTourTitle(),
                        p.getBookingsCount(),
                        p.getRevenueCents()))
                .toList();

        List<RecentBooking> recent = bookingRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(b -> new RecentBooking(
                        b.getReference(),
                        b.getLeadFirstName() + " " + b.getLeadLastName(),
                        b.getTour().getTitle(),
                        b.getStatus().name(),
                        b.getTotalCents(),
                        b.getCurrency(),
                        b.getCreatedAt()))
                .toList();

        return new DashboardResponse(kpis, series, topTours, recent);
    }
}