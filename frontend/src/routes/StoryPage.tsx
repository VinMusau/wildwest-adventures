import { Link } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import { photos } from "@/data/mock";

const VALUES: Array<[string, string, string]> = [
  [
    "Private by design",
    "Every journey is yours alone. Your own vehicle, your own guide, your own pace.",
  ],
  [
    "Local at the core",
    "We work with guides, camps and communities who have called these places home for generations.",
  ],
  [
    "Slow, on purpose",
    "Fewer stops, longer stays. The best moments often happen when nothing is scheduled at all.",
  ],
];

const GUIDES: Array<[string, string, string]> = [
  ["Samuel", "Kenya & Tanzania", photos.safari],
  ["Marta", "Patagonia", photos.mountain],
  ["Tendai", "Botswana & Zimbabwe", photos.elephant],
  ["Amara", "Islands & Coast", photos.island],
];

export default function StoryPage() {
  return (
    <main className="light-page">
      <Nav />

      <section
        className="story-hero"
        style={{ backgroundImage: `url(${photos.hero})` }}
      >
        <div className="story-hero-scrim" />
        <div className="story-hero-content">
          <span className="hero-kicker">
            <i /> Our story
          </span>
          <h1>
            We build journeys
            <br />
            for the curious.
          </h1>
          <p>
            Since 2019, we've taken small groups and private travellers into
            some of the world's last truly wild places — always with the people
            who know them best.
          </p>
        </div>
      </section>

      <section className="story">
        <div
          className="story-image"
          style={{ backgroundImage: `url(${photos.safari})` }}
        />
        <div className="story-copy">
          <span className="eyebrow">Why we exist</span>
          <h2>
            The rarest luxury
            <br />
            is feeling truly there.
          </h2>
          <p>
            We started Wild West Adventures because we were tired of watching
            travel become faster, cheaper, and more generic. The places we love
            deserved better — and so did the people visiting them.
          </p>
          <p>
            So we did the opposite. Fewer destinations. Smaller groups. Longer
            stays. Fewer things to tick off, more time to actually feel a place.
            Twelve years on, we still design every journey as if we were
            taking it ourselves.
          </p>
          <Link to="/journeys" className="text-link" style={{ marginTop: 24 }}>
            See our journeys <Icon name="arrow" />
          </Link>
        </div>
      </section>

      <section className="section story-values">
        <div className="section-head">
          <div>
            <span className="eyebrow">What we believe</span>
            <h2>Three things we don't compromise on.</h2>
          </div>
        </div>
        <div className="story-values-grid">
          {VALUES.map(([title, copy], i) => (
            <div className="story-value" key={title}>
              <span className="story-value-num">0{i + 1}</span>
              <h3>{title}</h3>
              <p>{copy}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="story-stats">
        <div className="story-stats-inner">
          <div>
            <strong>18</strong>
            <span>Years in the field</span>
          </div>
          <div>
            <strong>2,400+</strong>
            <span>Journeys designed</span>
          </div>
          <div>
            <strong>4.9/5</strong>
            <span>Average guest rating</span>
          </div>
          <div>
            <strong>100%</strong>
            <span>Carbon offset</span>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="section-head">
          <div>
            <span className="eyebrow">Who you'll travel with</span>
            <h2>Guides who call these places home.</h2>
            <p>
              Every journey is led by someone who grew up nearby, speaks the
              language, and knows where the light falls best at golden hour.
            </p>
          </div>
        </div>
        <div className="story-guides">
          {GUIDES.map(([name, region, image]) => (
            <div className="story-guide" key={name}>
              <div
                className="story-guide-image"
                style={{ backgroundImage: `url(${image})` }}
              />
              <strong>{name}</strong>
              <small>{region}</small>
            </div>
          ))}
        </div>
      </section>

      <section className="newsletter">
        <div>
          <span className="eyebrow">Come with us</span>
          <h2>
            Ready to see
            <br />
            the world differently?
          </h2>
        </div>
        <div>
          <p>
            Browse our current journeys, or tell us what you have in mind and
            we'll design something entirely yours.
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