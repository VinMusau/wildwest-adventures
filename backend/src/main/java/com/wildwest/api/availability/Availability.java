package com.wildwest.api.availability;

import com.wildwest.api.common.BaseEntity;
import com.wildwest.api.tour.Tour;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "availability")
public class Availability extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer booked;

    @Column(name = "price_cents")
    private Integer priceCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    public int remaining() {
        return capacity - booked;
    }

    public int effectivePriceCents() {
        return priceCents != null ? priceCents : tour.getPriceCents();
    }
}