package com.wildwest.api.tour;

import com.wildwest.api.common.BaseEntity;
import com.wildwest.api.destination.Destination;
import com.wildwest.api.travelstyle.TravelStyle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tours")
public class Tour extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String place;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(nullable = false)
    private String tag;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean featured;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tour_styles",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "style_id")
    )
    private Set<TravelStyle> styles = new HashSet<>();
}