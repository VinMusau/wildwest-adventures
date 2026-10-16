package com.wildwest.api.admin;

import java.time.LocalDate;
import java.util.List;

public final class AdminDashboardDtos {

    private AdminDashboardDtos() {}

    public record KpiSummary(
            long bookingsThisMonth,
            long bookingsLastMonth,
            long revenueThisMonthCents,
            long revenueLastMonthCents,
            long confirmedTotal,
            long cancelledTotal,
            long pendingTotal,
            long expiredTotal,
            long totalBookings
    ) {}

    public record RevenuePoint(LocalDate date, long revenueCents, long bookingsCount) {}

    public record TopTour(String slug, String title, long bookingsCount, long revenueCents) {}

    public record RecentBooking(
            String reference,
            String customerName,
            String tourTitle,
            String status,
            int totalCents,
            String currency,
            java.time.Instant createdAt
    ) {}

    public record DashboardResponse(
            KpiSummary kpis,
            List<RevenuePoint> revenueSeries,
            List<TopTour> topTours,
            List<RecentBooking> recentBookings
    ) {}
}