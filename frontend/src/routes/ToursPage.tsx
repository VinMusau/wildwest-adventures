import { useState } from "react";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import TourCard from "@/components/tour/TourCard";
import { tours } from "@/data/mock";

const FILTER_LABELS = [
  "Destination",
  "Travel style",
  "Duration",
  "Price per person",
  "Activity level",
  "Departure month",
];

export default function ToursPage() {
  const [filters, setFilters] = useState<string[]>(["East Africa", "7–14 days"]);
  const [showFilters, setShowFilters] = useState(false);

  const removeFilter = (value: string) => {
    setFilters(filters.filter((f) => f !== value));
  };

  return (
    <main className="light-page">
      <Nav />
      <div className="listing-hero">
        <span className="eyebrow">Curated journeys</span>
        <h1>Go where you feel most alive.</h1>
        <p>
          Explore privately guided and small-group journeys, each thoughtfully
          designed and ready to make your own.
        </p>
      </div>

      <div className="listing-shell">
        <aside className={`filters ${showFilters ? "mobile-open" : ""}`}>
          <div className="filter-title">
            <strong>Filter journeys</strong>
            <button onClick={() => setShowFilters(false)}>
              <Icon name="close" />
            </button>
          </div>

          {FILTER_LABELS.map((label, i) => (
            <div className="filter-group" key={label}>
              <button>
                <span>{label}</span>
                <Icon name={i < 2 ? "down" : "chevron"} size={17} />
              </button>
              {i === 0 && (
                <div className="checks">
                  <label>
                    <input type="checkbox" defaultChecked /> East Africa <small>18</small>
                  </label>
                  <label>
                    <input type="checkbox" /> Southern Africa <small>12</small>
                  </label>
                  <label>
                    <input type="checkbox" /> Latin America <small>9</small>
                  </label>
                </div>
              )}
            </div>
          ))}

          <Button onClick={() => setShowFilters(false)}>Show 36 journeys</Button>
        </aside>

        <section className="results">
          <div className="results-bar">
            <div>
              <strong>36 journeys</strong>
              <div className="chips">
                {filters.map((f) => (
                  <button key={f} onClick={() => removeFilter(f)}>
                    {f} <Icon name="close" size={14} />
                  </button>
                ))}
              </div>
            </div>
            <div>
              <button className="filter-mobile" onClick={() => setShowFilters(true)}>
                <Icon name="sliders" /> Filters
              </button>
              <button className="sort">
                Sort: <strong>Recommended</strong> <Icon name="down" size={16} />
              </button>
              <button className="square active">
                <Icon name="map" />
              </button>
            </div>
          </div>

          <div className="tour-grid listing">
            {tours.map((tour) => (
              <TourCard key={tour.slug} tour={tour} />
            ))}
          </div>

          <Button variant="secondary" className="load-more">
            Load more journeys
          </Button>
        </section>
      </div>
      <Footer />
    </main>
  );
}