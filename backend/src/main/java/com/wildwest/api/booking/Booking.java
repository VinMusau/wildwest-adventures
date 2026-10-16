package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.common.BaseEntity;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    public enum Status { PENDING, CONFIRMED, CANCELLED, EXPIRED }

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_id", nullable = false)
    private Availability availability;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "travelers_count", nullable = false)
    private Integer travelersCount;

    @Column(name = "price_cents_per_person", nullable = false)
    private Integer priceCentsPerPerson;

    @Column(name = "subtotal_cents", nullable = false)
    private Integer subtotalCents;

    @Column(name = "addons_total_cents", nullable = false)
    private Integer addonsTotalCents;

    @Column(name = "total_cents", nullable = false)
    private Integer totalCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "lead_first_name", nullable = false)
    private String leadFirstName;

    @Column(name = "lead_last_name", nullable = false)
    private String leadLastName;

    @Column(name = "lead_email", nullable = false)
    private String leadEmail;

    @Column(name = "lead_phone")
    private String leadPhone;

    @Column(name = "lead_country")
    private String leadCountry;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<BookingTraveler> travelers = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<BookingAddon> addons = new ArrayList<>();

    @Column(name = "stripe_payment_intent_id", length = 120)
    private String stripePaymentIntentId;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payment_method", length = 40)
    private String paymentMethod;

    @Column(name = "paystack_reference", length = 120)
    private String paystackReference;

    @Column(name = "payment_provider", length = 20)
    private String paymentProvider = "STRIPE";

        @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "refund_reference", length = 120)
    private String refundReference;

    @Column(name = "refund_amount_cents")
    private Integer refundAmountCents;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;
}