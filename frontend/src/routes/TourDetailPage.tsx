import { useState } from "react";
import { Link, useParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import Rating from "@/components/ui/Rating";
import SectionHead from "@/components/tour/SectionHead";
import TourCard from "@/components/tour/TourCard";
import BookingCard from "@/components/tour/BookingCard";
import { useTour } from "@/features/tours/useTour";
import { useTours } from "@/features/tours/useTours";
import { formatPrice } from "@/lib/format";
import TourDetailSkeleton from "@/components/tour/TourDetailSkeleton";

export default function TourDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const { data: tour, isLoading, error } = useTour(slug);
  const [openDay, setOpenDay] = useState<number>(1);

  const { data: related = [] } = useTours(
    tour ? { destination: tour.destinationSlug } : {}
  );

  if (isLoading) {
    return (
      <main className="light-page">
        <Nav />
        <TourDetailSkeleton />
        <Footer />
      </main>
    );
  }

   if (error || !tour) {
     return (
       <main className="light-page">
         <Nav />
         <div
           className="section"
           style={{
             minHeight: "60vh",
             display: "flex",
             justifyContent: "center",
             alignItems: "center",
           }}
         >
           <div className="empty-state" style={{ border: "none" }}>
             <div className="empty-state-icon">
               <Icon name="map" size={32} />
             </div>
             <h3>Journey not found</h3>
             <p>
               The journey you're looking for doesn't exist or has been moved.
             </p>
             <Link to="/journeys" className="button primary">
               Back to all journeys
             </Link>
           </div>
         </div>
         <Footer />
       </main>
     );
   }

  const gallery = tour.images.length > 0
    ? tour.images
    : [{ imageUrl: tour.imageUrl, altText: tour.title }];

  const relatedTours = related.filter((t) => t.slug !== tour.slug).slice(0, 3);

  return (
    <main className="light-page">
      <Nav />

      <div className="breadcrumbs">
        <Link to="/">Home</Link>
        <span>/</span>
        <Link to="/journeys">Journeys</Link>
        <span>/</span>
        <span>{tour.place}</span>
      </div>

      <section className="detail-title">
        <div>
          <span className="eyebrow">
            {tour.tag} · {tour.place}
          </span>
          <h1>{tour.title}</h1>
        </div>
        <div>
          <Rating value={tour.rating.toFixed(1)} reviews={tour.reviewCount} />
          <p>
            {tour.durationDays} days · Private journey
          </p>
        </div>
      </section>

      <section className="gallery">
        {gallery.slice(0, 3).map((img, i) => (
          <img key={i} src={img.imageUrl} alt={img.altText ?? tour.title} />
        ))}
        {tour.images.length > 3 && (
          <button>View all {tour.images.length} photos</button>
        )}
      </section>

      <div className="detail-layout">
        <div className="detail-main">
          <p className="lead">
            A thoughtfully crafted journey through {tour.place}, private
            throughout and unhurried by design.
          </p>

          <div className="facts">
            <div>
              <Icon name="clock" />
              <span>{tour.durationDays} days</span>
            </div>
            <div>
              <Icon name="users" />
              <span>2–8 guests</span>
            </div>
            <div>
              <Icon name="pin" />
              <span>{tour.destinationName}</span>
            </div>
            <div>
              <Icon name="star" />
              <span>Easy pace</span>
            </div>
          </div>

          <section className="content-section">
            <span className="eyebrow">The experience</span>
            <h2>Wild places, at your own pace.</h2>
            <p>
              Wake to the sound of the bush, spend unhurried days with expert
              guides and return each evening to intimate camps chosen for
              their character and sense of place.
            </p>
            <div className="highlights">
              <div>
                <strong>Private throughout</strong>
                <span>Your own vehicle and guide</span>
              </div>
              <div>
                <strong>Front-row access</strong>
                <span>Stay inside private reserves</span>
              </div>
              <div>
                <strong>Travel with purpose</strong>
                <span>Conservation-led camps</span>
              </div>
            </div>
          </section>

          <section className="content-section">
            <span className="eyebrow">Your itinerary</span>
            <h2>
              {tour.durationDays} days, one remarkable story.
            </h2>
            {tour.itinerary.map((day) => {
              const isOpen = openDay === day.dayNumber;
              return (
                <div
                  className={`accordion ${isOpen ? "open" : ""}`}
                  key={day.dayNumber}
                >
                  <button
                    onClick={() => setOpenDay(isOpen ? 0 : day.dayNumber)}
                  >
                    <span>DAY {String(day.dayNumber).padStart(2, "0")}</span>
                    <strong>{day.title}</strong>
                    <Icon name="down" />
                  </button>
                  {isOpen && (
                    <div>
                      <p>{day.description}</p>
                      {(day.accommodation || day.mealsIncluded) && (
                        <span>
                          {day.accommodation && `Accommodation: ${day.accommodation}`}
                          {day.accommodation && day.mealsIncluded && " · "}
                          {day.mealsIncluded}
                        </span>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </section>

          <section className="inclusions content-section">
            <div>
              <span className="eyebrow">All taken care of</span>
              <h2>What's included</h2>
            </div>
            <ul>
              {tour.inclusions.map((item) => (
                <li key={item}>
                  <Icon name="check" />
                  {item}
                </li>
              ))}
            </ul>
          </section>
        </div>

        <BookingCard
          priceFrom={formatPrice(tour.priceCents, tour.currency)}
          bookHref={`/book/${tour.slug}`}
        />
      </div>

      {relatedTours.length > 0 && (
        <section className="section related">
          <SectionHead
            eyebrow="Continue exploring"
            title="You might also like"
          />
          <div className="tour-grid">
            {relatedTours.map((t) => (
              <TourCard key={t.slug} tour={t} />
            ))}
          </div>
        </section>
      )}

      <div className="mobile-book">
        <span>
          From <strong>{formatPrice(tour.priceCents, tour.currency)}</strong>
        </span>
        <Link to={`/book/${tour.slug}`} className="button primary">
          Check availability
        </Link>
      </div>

      <Footer />
    </main>
  );
}