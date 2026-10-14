import { useState } from "react";
import { Link, useParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import Button from "@/components/ui/Button";
import Rating from "@/components/ui/Rating";
import SectionHead from "@/components/tour/SectionHead";
import TourCard from "@/components/tour/TourCard";
import BookingCard from "@/components/tour/BookingCard";
import { useTours } from "@/features/tours/useTours";
import { photos } from "@/data/mock";

const ITINERARY = [
  ["Arrival in Arusha", "Settle into a peaceful coffee estate beneath Mount Meru."],
  ["Into the Serengeti", "Fly into the wild heart of the Serengeti for your first game drive."],
  ["Following the herds", "A full day tracking the migration with your private naturalist."],
  ["The Maasai Mara", "Cross into Kenya and settle into a secluded riverside camp."],
];

const INCLUSIONS = [
  "Private guiding and game drives",
  "Handpicked luxury accommodation",
  "All internal flights and transfers",
  "Daily meals and selected drinks",
  "24/7 in-country support",
];

const FACTS: Array<[string, string]> = [
  ["clock", "12 days"],
  ["users", "2–8 guests"],
  ["pin", "6 locations"],
  ["star", "Easy pace"],
];

export default function TourDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const [openDay, setOpenDay] = useState<number>(1);

  // Phase 1: fetch tour by slug. For now, use the first tour as a placeholder.
  const tour = tours.find((t) => t.slug === slug) ?? tours[0];

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
          <Rating value={tour.rating} reviews={tour.reviews} />
          <p>{tour.days} · Private journey</p>
        </div>
      </section>

      <section className="gallery">
        <img src={photos.hero} alt="Elephants crossing the Serengeti" />
        <img src={photos.safari} alt="Safari game drive" />
        <img src={photos.elephant} alt="Elephant on the savannah" />
        <button>View all 28 photos</button>
      </section>

      <div className="detail-layout">
        <div className="detail-main">
          <p className="lead">
            Follow the rhythm of the wild across two extraordinary countries —
            from the quiet drama of the Serengeti to the abundant plains of the
            Maasai Mara.
          </p>

          <div className="facts">
            {FACTS.map(([icon, text]) => (
              <div key={text}>
                <Icon name={icon as never} />
                <span>{text}</span>
              </div>
            ))}
          </div>

          <section className="content-section">
            <span className="eyebrow">The experience</span>
            <h2>Wild Africa, at your own pace.</h2>
            <p>
              Wake to the sound of the bush, spend unhurried days with expert
              guides and return each evening to intimate camps chosen for their
              character and sense of place.
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
            <h2>Twelve days, one remarkable story.</h2>
            {ITINERARY.map(([title, description], i) => {
              const day = i + 1;
              const isOpen = openDay === day;
              return (
                <div className={`accordion ${isOpen ? "open" : ""}`} key={title}>
                  <button onClick={() => setOpenDay(isOpen ? 0 : day)}>
                    <span>DAY {String(day).padStart(2, "0")}</span>
                    <strong>{title}</strong>
                    <Icon name="down" />
                  </button>
                  {isOpen && (
                    <div>
                      <p>{description}</p>
                      <span>Accommodation: Legendary Lodge · Meals included</span>
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
              {INCLUSIONS.map((item) => (
                <li key={item}>
                  <Icon name="check" />
                  {item}
                </li>
              ))}
            </ul>
          </section>
        </div>

        <BookingCard priceFrom={tour.price} bookHref={`/book/${tour.slug}`} />
      </div>

      <section className="section related">
        <SectionHead eyebrow="Continue exploring" title="You might also like" />
        <div className="tour-grid">
          {tours.slice(1, 4).map((t) => (
            <TourCard key={t.slug} tour={t} />
          ))}
        </div>
      </section>

      <div className="mobile-book">
        <span>
          From <strong>{tour.price}</strong>
        </span>
        <Link to={`/book/${tour.slug}`} className="button primary">
          Check availability
        </Link>
      </div>

      <Footer />
    </main>
  );
}