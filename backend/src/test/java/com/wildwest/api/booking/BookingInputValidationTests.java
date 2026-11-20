package com.wildwest.api.booking;

import com.wildwest.api.auth.AuthDtos.RegisterRequest;
import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingInputValidationTests {

    private static final String TOUR = "maldives-private-island";
    private static final TravelerInput ADULT = new TravelerInput("Ann", "Lee", "ADULT");
    private static final LeadInput LEAD = new LeadInput("Ann", "Lee", "ann@example.com", "+254700000000", "Kenya");

    @Autowired Validator validator;
    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired AvailabilityRepository availabilityRepository;
    @Autowired UserRepository userRepository;

    @Test
    void validRequestPasses() {
        assertThat(violations(request(List.of(ADULT), LEAD, List.of("airport-transfer")))).isEmpty();
    }

    @Test
    void overlongPhoneAndCountryAreRejectedWithAFieldMessage() {
        LeadInput lead = new LeadInput("Ann", "Lee", "ann@example.com", "9".repeat(51), "x".repeat(101));
        assertThat(violations(request(List.of(ADULT), lead, null)))
                .containsKeys("lead.phone", "lead.country");
    }

    @Test
    void emailWithoutADottedDomainIsRejected() {
        LeadInput lead = new LeadInput("Ann", "Lee", "a@b", null, null);
        assertThat(violations(request(List.of(ADULT), lead, null)))
                .containsEntry("lead.email", "must be a valid email address");

        RegisterRequest register = new RegisterRequest("a@b", "password123", "Ann", "Lee");
        assertThat(violations(register)).containsOnlyKeys("email");
    }

    @Test
    void unknownTravelerTypeIsRejectedInsteadOfSilentlyBecomingAdult() {
        TravelerInput infant = new TravelerInput("Baby", "Lee", "INFANT");
        assertThat(violations(request(List.of(infant), LEAD, null)))
                .containsEntry("travelers[0].travelerType", "must be ADULT or CHILD");
        assertThat(violations(request(List.of(new TravelerInput("Kid", "Lee", "child")), LEAD, null))).isEmpty();
    }

    @Test
    void repeatedAddOnIsChargedOnce() {
        User user = new User();
        user.setEmail("validation-test-" + UUID.randomUUID() + "@example.test");
        user.setPasswordHash("not-used");
        user.setFirstName("Validation");
        user.setLastName("Test");
        user.setRole(UserRole.USER);
        User saved = userRepository.save(user);
        List<Availability> dates = availabilityRepository.findByTourSlugOrderByDepartureDateAsc(TOUR);
        Availability departure = dates.get(dates.size() - 1);
        int bookedBefore = departure.getBooked();
        try {
            BookingDto once = bookingService.create(saved, new CreateBookingRequest(TOUR, departure.getDepartureDate(),
                    List.of(ADULT), LEAD, List.of("airport-transfer")));
            BookingDto thrice = bookingService.create(saved, new CreateBookingRequest(TOUR, departure.getDepartureDate(),
                    List.of(ADULT), LEAD, List.of("airport-transfer", "airport-transfer", "airport-transfer")));

            assertThat(thrice.addons()).hasSize(1);
            assertThat(thrice.addonsTotalCents()).isEqualTo(once.addonsTotalCents());
        } finally {
            bookingRepository.deleteAll(bookingRepository.findByUserIdOrderByCreatedAtDesc(saved.getId()));
            userRepository.delete(saved);
            Availability a = availabilityRepository.findById(departure.getId()).orElseThrow();
            a.setBooked(bookedBefore);
            availabilityRepository.save(a);
        }
    }

    private static CreateBookingRequest request(List<TravelerInput> travelers, LeadInput lead, List<String> addons) {
        return new CreateBookingRequest(TOUR, LocalDate.now().plusMonths(2), travelers, lead, addons);
    }

    /** Field path to message. */
    private Map<String, String> violations(Object o) {
        return validator.validate(o).stream().collect(Collectors.toMap(
                c -> c.getPropertyPath().toString(), ConstraintViolation::getMessage, (a, b) -> a));
    }
}
