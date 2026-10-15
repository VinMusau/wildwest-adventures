package com.wildwest.api.booking;

import com.wildwest.api.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "booking_addons")
public class BookingAddon extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false)
    private String label;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;
}
