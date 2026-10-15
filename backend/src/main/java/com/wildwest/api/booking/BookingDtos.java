package com.wildwest.api.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
            @NotEmpty @Valid List<TravelerInput> travelers,
            @NotNull @Valid LeadInput lead,
            List<String> addons
    ) {}

    public record TravelerInput(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            String travelerType
    ) {}

    public record LeadInput(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @Email @NotBlank String email,
            String phone,
            String country
    ) {}

    // ---------- responses ----------

    public record TravelerDto(
            UUID id,
            String firstName,
            String lastName,
            String travelerType
    ) {
        public static TravelerDto from(BookingTraveler traveler) {
            return new TravelerDto(
                    traveler.getId(),
                    traveler.getFirstName(),
                    traveler.getLastName(),
                    traveler.getTravelerType().name()
            );
        }
    }

    public record AddonDto(UUID id, String code, String label, int priceCents) {
        public static AddonDto from(BookingAddon addon) {
            return new AddonDto(
                    addon.getId(),
                    addon.getCode(),
                    addon.getLabel(),
                    addon.getPriceCents()
            );
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
            List<AddonDto> addons
    ) {
        public static BookingDto from(Booking booking) {
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
                    booking.getAddons().stream().map(AddonDto::from).toList()
            );
        }
    }
}