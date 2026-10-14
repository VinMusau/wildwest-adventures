package com.wildwest.api.tour;

import com.wildwest.api.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tour_inclusions")
public class TourInclusion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false)
    private String label;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}