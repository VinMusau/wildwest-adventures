package com.wildwest.api.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.wildwest.api.admin.AdminTourDtos.TourAdminDetail;
import com.wildwest.api.admin.AdminTourDtos.TourImageInput;
import com.wildwest.api.admin.AdminTourDtos.TourWriteRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminTourGalleryTests {

    private static final String TOUR = "torres-del-paine";

    @Autowired
    AdminTourService service;

    private TourAdminDetail original;

    @BeforeEach
    void setUp() {
        original = service.get(TOUR);
    }

    @AfterEach
    void tearDown() {
        service.update(TOUR, request(original, original.images()));
    }

    @Test
    void savingReplacesTheGalleryInTheGivenOrder() {
        List<TourImageInput> gallery = List.of(
                new TourImageInput("https://img.example/b.jpg", "Glacier at dawn"),
                new TourImageInput("https://img.example/a.jpg", "  "),
                new TourImageInput("https://img.example/c.jpg", null));

        TourAdminDetail saved = service.update(TOUR, request(original, gallery));

        assertThat(saved.images())
                .extracting(TourImageInput::imageUrl)
                .containsExactly("https://img.example/b.jpg", "https://img.example/a.jpg", "https://img.example/c.jpg");
        assertThat(saved.images()).extracting(TourImageInput::altText).containsExactly("Glacier at dawn", null, null);

        // Reordering and removing: what's sent is what's kept.
        TourAdminDetail reordered = service.update(TOUR, request(original, List.of(gallery.get(2), gallery.get(0))));
        assertThat(reordered.images())
                .extracting(TourImageInput::imageUrl)
                .containsExactly("https://img.example/c.jpg", "https://img.example/b.jpg");
    }

    @Test
    void leavingImagesOutKeepsTheGallery() {
        service.update(TOUR, request(original, List.of(new TourImageInput("https://img.example/keep.jpg", null))));

        TourAdminDetail saved = service.update(TOUR, request(original, null));

        assertThat(saved.images()).extracting(TourImageInput::imageUrl).containsExactly("https://img.example/keep.jpg");
    }

    @Test
    void anEmptyListClearsTheGallery() {
        service.update(TOUR, request(original, List.of(new TourImageInput("https://img.example/x.jpg", null))));

        assertThat(service.update(TOUR, request(original, List.of())).images()).isEmpty();
    }

    /** The tour as it is, with the given gallery. */
    private static TourWriteRequest request(TourAdminDetail t, List<TourImageInput> images) {
        return new TourWriteRequest(
                t.title(),
                t.slug(),
                t.place(),
                t.destinationSlug(),
                t.durationDays(),
                t.priceCents(),
                t.currency(),
                t.tag(),
                t.imageUrl(),
                t.featured(),
                t.displayOrder(),
                t.styleSlugs(),
                t.itinerary(),
                t.inclusions(),
                t.tourType(),
                images);
    }
}
