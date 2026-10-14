import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import Button from "@/components/ui/Button";
import Rating from "@/components/ui/Rating";
import SectionHead from "@/components/tour/SectionHead";
import SearchBox from "@/components/tour/SearchBox";
import TourCard from "@/components/tour/TourCard";
import { photos } from "@/data/mock";
import { useTravelStyles } from "@/features/travel-styles/useTravelStyles";
import { useDestinations } from "@/features/destinations/useDestinations";
import { useTours } from "@/features/tours/useTours";

export default function HomePage() {
  const scrollToStyles = () => {
    document.getElementById("styles")?.scrollIntoView({ behavior: "smooth" });
  };

  const { data: travelStyles = [], isLoading: stylesLoading } = useTravelStyles();
  const { data: destinations = [], isLoading: destinationsLoading } = useDestinations();
  const { data: featuredTours = [], isLoading: toursLoading } = useTours({ featured: true });
  return (
    <main>
      <section
        className="hero"
        style={{ backgroundImage: `url(${photos.hero})` }}
      >
        <Nav light />
        <div className="hero-scrim" />
        <div className="hero-content">
          <span className="hero-kicker">
            <i /> JOURNEYS WORTH TAKING
          </span>
          <h1>
            Find the wild
            <br />
            within reach.
          </h1>
          <p>
            Thoughtful journeys into the world's most remarkable places, shaped
            by people who know them by heart.
          </p>
          <SearchBox />
        </div>
        <button className="scroll-cue" onClick={scrollToStyles}>
          <span>Discover more</span>
          <Icon name="down" />
        </button>
      </section>

      <section className="section intro" id="styles">
        <SectionHead
          eyebrow="Travel your way"
          title="What calls you?"
          copy="However you imagine your time away, we'll make it deeply personal and entirely unforgettable."
        />
        <div className="style-grid">
          {stylesLoading ? (
            <p className="text-muted">Loading styles…</p>
          ) : (
            travelStyles.map((style, index) => (
              <button
                className={`style-card ${index < 2 ? "large" : ""}`}
                key={style.slug}
                style={{ backgroundImage: `url(${style.imageUrl})` }}
              >
                <span className="image-shade" />
                <span className="style-content">
                  <small>0{style.displayOrder}</small>
                  <strong>{style.title}</strong>
                  <em>{style.description}</em>
                </span>
              </button>
            ))
          )}
        </div>
      </section>

      <section className="section featured">
        <SectionHead
          eyebrow="Our current favourites"
          title="Journeys to inspire"
          copy="A small collection of remarkable ways to see the world, each with room to make it your own."
          action="View all journeys"
        />
        <div className="tour-grid">
          {toursLoading ? (
            <p className="text-muted">Loading tours…</p>
          ) : (
            featuredTours
              .slice(0, 3)
              .map((tour) => <TourCard tour={tour} key={tour.slug} />)
          )}
        </div>
      </section>

      <section className="story">
        <div
          className="story-image"
          style={{ backgroundImage: `url(${photos.safari})` }}
        >
          <button className="play">
            <Icon name="play" />
          </button>
        </div>
        <div className="story-copy">
          <span className="eyebrow">Why Wild West</span>
          <h2>The rarest luxury is feeling truly there.</h2>
          <p>
            We believe the best journeys are measured not in miles, but in
            moments of genuine connection. With private access, exceptional
            local guides and unhurried time, we create space for those moments
            to find you.
          </p>
          <dl>
            <div>
              <dt>24/7</dt>
              <dd>care while you travel</dd>
            </div>
            <div>
              <dt>5 yrs</dt>
              <dd>of local expertise</dd>
            </div>
            <div>
              <dt>100%</dt>
              <dd>carbon offset</dd>
            </div>
          </dl>
          <Button variant="secondary">
            Our approach <Icon name="arrow" />
          </Button>
        </div>
      </section>

      <section className="section destinations">
        <SectionHead
          eyebrow="Explore the world"
          title="Places with a story to tell"
          action="All destinations"
        />
        <div className="destination-grid">
          {destinationsLoading ? (
            <p className="text-muted">Loading destinations…</p>
          ) : (
            destinations.map((destination) => (
              <button
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
              </button>
            ))
          )}
        </div>
      </section>

      <section className="quote-section">
        <span className="quote-mark">"</span>
        <blockquote>
          They didn't just show us The Maasai Mara. They made us feel part of
          it.
        </blockquote>
        <p>
          Every detail felt effortless, every day held something we could never
          have planned ourselves.
        </p>
        <Rating value="5.0" />
        <strong className="quote-name">THE MARSHALL FAMILY · LONDON</strong>
      </section>

      <section className="newsletter">
        <div>
          <span className="eyebrow">The field notes</span>
          <h2>
            A little wildness,
            <br />
            delivered monthly.
          </h2>
        </div>
        <div>
          <p>
            Stories from the road, new journeys and places worth knowing about.
          </p>
          <form onSubmit={(e) => e.preventDefault()}>
            <input
              type="email"
              placeholder="Your email address"
              aria-label="Email address"
            />
            <Button type="submit">
              Sign me up <Icon name="arrow" />
            </Button>
          </form>
          <small>By subscribing you agree to our privacy policy.</small>
        </div>
      </section>

      <Footer />
    </main>
  );
}