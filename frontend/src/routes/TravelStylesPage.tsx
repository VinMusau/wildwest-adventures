import { Link } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import { useTravelStyles } from "@/features/travel-styles/useTravelStyles";

export default function TravelStylesPage() {
  const { data: styles = [], isLoading } = useTravelStyles();

  return (
    <main className="light-page">
      <Nav />

      <div className="listing-hero">
        <span className="eyebrow">Travel your way</span>
        <h1>What calls you?</h1>
        <p>
          However you imagine your time away, we'll make it deeply personal and
          entirely unforgettable.
        </p>
      </div>

      <section className="section" style={{ paddingTop: 72 }}>
        {isLoading ? (
          <p className="text-muted">Loading styles…</p>
        ) : (
          <div className="style-grid-full">
            {styles.map((style) => (
              <Link
                to={`/journeys?style=${style.slug}`}
                className="style-card"
                style={{ backgroundImage: `url(${style.imageUrl})` }}
                key={style.slug}
              >
                <span className="image-shade" />
                <span className="style-content">
                  <small>0{style.displayOrder}</small>
                  <strong>{style.title}</strong>
                  <em>{style.description}</em>
                </span>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="newsletter">
        <div>
          <span className="eyebrow">Still deciding?</span>
          <h2>
            Tell us what you have in mind,
            <br />
            we'll do the rest.
          </h2>
        </div>
        <div>
          <p>
            A short conversation with one of our specialists is often all it
            takes to design the right journey for you.
          </p>
          <Link
            to="/journeys"
            className="button primary"
            style={{ marginTop: 20 }}
          >
            Browse all journeys <Icon name="arrow" />
          </Link>
        </div>
      </section>

      <Footer />
    </main>
  );
}