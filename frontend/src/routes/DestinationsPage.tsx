import { Link } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import { useDestinations } from "@/features/destinations/useDestinations";

export default function DestinationsPage() {
  const { data: destinations = [], isLoading } = useDestinations();

  return (
    <main className="light-page">
      <Nav />

      <div className="listing-hero">
        <span className="eyebrow">Where we travel</span>
        <h1>Places with a story to tell.</h1>
        <p>
          Four regions, hundreds of journeys. Each one designed by people who
          know these landscapes like the backs of their hands.
        </p>
      </div>

      <section className="section" style={{ paddingTop: 72 }}>
        {isLoading ? (
          <p className="text-muted">Loading destinations…</p>
        ) : (
          <div className="destination-grid">
            {destinations.map((destination) => (
              <Link
                to={`/journeys?destination=${destination.slug}`}
                className="destination"
                style={{ backgroundImage: `url(${destination.imageUrl})` }}
                key={destination.slug}
              >
                <span />
                <div>
                  <small>{destination.journeyCount} journeys</small>
                  <strong>{destination.name}</strong>
                  <Icon name="arrow" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="quote-section">
        <span className="quote-mark">"</span>
        <blockquote>
          Not sure where to start? Tell us how you like to travel.
        </blockquote>
        <p>
          Our destination specialists have spent years in the field. A short
          conversation is often all it takes to find the right place.
        </p>
        <Link
          to="/journeys"
          className="button primary"
          style={{ marginTop: 28 }}
        >
          Browse all journeys <Icon name="arrow" />
        </Link>
      </section>

      <Footer />
    </main>
  );
}