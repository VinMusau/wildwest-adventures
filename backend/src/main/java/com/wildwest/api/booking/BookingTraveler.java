package com.wildwest.api.booking;

import com.wildwest.api.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "booking_travelers")
public class BookingTraveler extends BaseEntity {

    public enum Type { ADULT, CHILD }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "traveler_type", nullable = false, length = 20)
    private Type travelerType = Type.ADULT;
}