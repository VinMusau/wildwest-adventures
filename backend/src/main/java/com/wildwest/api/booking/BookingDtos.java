package com.wildwest.api.booking;

import com.wildwest.api.common.Validation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BookingDtos {

    private BookingDtos() {}

    // ---------- requests ----------

    public record CreateBookingRequest(
            @NotBlank String tourSlug,
            @NotNull LocalDate departureDate,
            @NotEmpty List<@Valid TravelerInput> travelers,
            @NotNull @Valid LeadInput lead,
            List<@NotBlank @Size(max = 60) String> addons) {}

    public record TravelerInput(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,

            @Pattern(regexp = "(?i)ADULT|CHILD", message = "must be ADULT or CHILD")
            String travelerType) {}

    public record LeadInput(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,

            @Email(regexp = Validation.EMAIL, message = Validation.EMAIL_MESSAGE) @NotBlank @Size(max = 255)
            String email,

            @Size(max = 50) String phone,
            @Size(max = 100) String country) {}

    // ---------- responses ----------

    public record TravelerDto(UUID id, String firstName, String lastName, String travelerType) {
        public static TravelerDto from(BookingTraveler traveler) {
            return new TravelerDto(
                    traveler.getId(),
                    traveler.getFirstName(),
                    traveler.getLastName(),
                    traveler.getTravelerType().name());
        }
    }

    public record AddonDto(UUID id, String code, String label, int priceCents) {
        public static AddonDto from(BookingAddon addon) {
            return new AddonDto(addon.getId(), addon.getCode(), addon.getLabel(), addon.getPriceCents());
        }
    }

    public record BookingDto(
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
            String leadFirstName,
            String leadLastName,
            String leadEmail,
            String leadPhone,
            String leadCountry,
            Instant expiresAt,
            Instant createdAt,
            List<TravelerDto> travelers,
            List<AddonDto> addons,
            // true when the trip has ended and the customer hasn't reviewed it yet
            boolean canReview,
            // PENDING, APPROVED or REJECTED once the customer has reviewed the trip; else null
            String reviewStatus) {
        public static BookingDto from(Booking booking) {
            return from(booking, false, null);
        }

        public static BookingDto from(Booking booking, boolean canReview, String reviewStatus) {
            return new BookingDto(
                    booking.getId(),
                    booking.getReference(),
                    booking.getStatus().name(),
                    booking.getTour().getSlug(),
                    booking.getTour().getTitle(),
                    booking.getTour().getPlace(),
                    booking.getTour().getImageUrl(),
                    booking.getDepartureDate(),
                    booking.getTravelersCount(),
                    booking.getPriceCentsPerPerson(),
                    booking.getSubtotalCents(),
                    booking.getAddonsTotalCents(),
                    booking.getTotalCents(),
                    booking.getCurrency(),
                    booking.getLeadFirstName(),
                    booking.getLeadLastName(),
                    booking.getLeadEmail(),
                    booking.getLeadPhone(),
                    booking.getLeadCountry(),
                    booking.getExpiresAt(),
                    booking.getCreatedAt(),
                    booking.getTravelers().stream().map(TravelerDto::from).toList(),
                    booking.getAddons().stream().map(AddonDto::from).toList(),
                    canReview,
                    reviewStatus);
        }
    }
}
