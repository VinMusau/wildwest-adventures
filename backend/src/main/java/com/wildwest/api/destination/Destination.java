package com.wildwest.api.destination;

import com.wildwest.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "destinations")
public class Destination extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "journey_count", nullable = false)
    private Integer journeyCount;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}