package com.wildwest.api.admin;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingAddon;
import com.wildwest.api.booking.BookingTraveler;
import com.wildwest.api.booking.BookingDtos.AddonDto;
import com.wildwest.api.booking.BookingDtos.TravelerDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AdminBookingDtos {

    private AdminBookingDtos() {}

    public record BookingAdminSummary(
            UUID id,
            String reference,
            String status,
            String customerName,
            String customerEmail,
            String tourSlug,
            String tourTitle,
            LocalDate departureDate,
            int travelersCount,
            int totalCents,
            String currency,
            String paymentProvider,
            Instant createdAt,
            Instant paidAt,
            Instant refundedAt
    ) {
        public static BookingAdminSummary from(Booking b) {
            return new BookingAdminSummary(
                    b.getId(),
                    b.getReference(),
                    b.getStatus().name(),
                    b.getLeadFirstName() + " " + b.getLeadLastName(),
                    b.getLeadEmail(),
                    b.getTour().getSlug(),
                    b.getTour().getTitle(),
                    b.getDepartureDate(),
                    b.getTravelersCount(),
                    b.getTotalCents(),
                    b.getCurrency(),
                    b.getPaymentProvider(),
                    b.getCreatedAt(),
                    b.getPaidAt(),
                    b.getRefundedAt()
            );
        }
    }

    public record BookingAdminDetail(
            UUID id,
            String reference,
            String status,
            String tourSlug,
            String tourTitle,
            String tourPlace,
            String tourImageUrl,
            LocalDate departureDate,
            int travelersCount,
            int priceCentsPerPerson,
            int subtotalCents,
            int addonsTotalCents,
            int totalCents,
            String currency,
            String paymentProvider,
            String paystackReference,
            String stripePaymentIntentId,
            Instant paidAt,
            Instant expiresAt,
            Instant refundedAt,
            String refundReference,
            Integer refundAmountCents,
            Instant cancelledAt,
            String cancelReason,
            String leadFirstName,
            String leadLastName,
            String leadEmail,
            String leadPhone,
            String leadCountry,
            Instant createdAt,
            List<TravelerDto> travelers,
            List<AddonDto> addons
    ) {
        public static BookingAdminDetail from(Booking b) {
            return new BookingAdminDetail(
                    b.getId(),
                    b.getReference(),
                    b.getStatus().name(),
                    b.getTour().getSlug(),
                    b.getTour().getTitle(),
                    b.getTour().getPlace(),
                    b.getTour().getImageUrl(),
                    b.getDepartureDate(),
                    b.getTravelersCount(),
                    b.getPriceCentsPerPerson(),
                    b.getSubtotalCents(),
                    b.getAddonsTotalCents(),
                    b.getTotalCents(),
                    b.getCurrency(),
                    b.getPaymentProvider(),
                    b.getPaystackReference(),
                    b.getStripePaymentIntentId(),
                    b.getPaidAt(),
                    b.getExpiresAt(),
                    b.getRefundedAt(),
                    b.getRefundReference(),
                    b.getRefundAmountCents(),
                    b.getCancelledAt(),
                    b.getCancelReason(),
                    b.getLeadFirstName(),
                    b.getLeadLastName(),
                    b.getLeadEmail(),
                    b.getLeadPhone(),
                    b.getLeadCountry(),
                    b.getCreatedAt(),
                    b.getTravelers().stream().map(TravelerDto::from).toList(),
                    b.getAddons().stream().map(AddonDto::from).toList()
            );
        }
    }

    public record CancelRequest(String reason) {}

    public record RefundRequest(Integer amountCents) {}
}