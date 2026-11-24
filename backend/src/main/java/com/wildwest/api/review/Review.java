package com.wildwest.api.review;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.common.BaseEntity;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A customer's review of a trip they took. One per booking. */
@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    public enum Status {
        /** Waiting for an admin. Not shown publicly. */
        PENDING,
        /** Shown publicly and counted in the tour's rating. */
        APPROVED,
        /** Hidden. */
        REJECTED
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 1 to 5. A SMALLINT column. */
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer rating;

    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** As shown publicly, e.g. "Amina O.". */
    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_country")
    private String authorCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "moderated_at")
    private Instant moderatedAt;
}
