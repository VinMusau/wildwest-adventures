import { useMemo, useState } from "react";
import { useSearchParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import TourCard from "@/components/tour/TourCard";
import { useTours, type TourFilters } from "@/features/tours/useTours";
import { useDestinations } from "@/features/destinations/useDestinations";
import { useTravelStyles } from "@/features/travel-styles/useTravelStyles";
import TourCardSkeleton from "@/components/tour/TourCardSkeleton";

const SORT_OPTIONS: Array<[string, string]> = [
  ["recommended", "Recommended"],
  ["price-asc", "Price (low to high)"],
  ["price-desc", "Price (high to low)"],
  ["rating", "Top rated"],
  ["duration-asc", "Duration (shortest)"],
  ["duration-desc", "Duration (longest)"],
];

export default function ToursPage() {
  const [showFilters, setShowFilters] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();

  // URL is the source of truth for filters
  const filters: TourFilters = useMemo(
    () => ({
      destination: searchParams.get("destination") ?? undefined,
      style: searchParams.get("style") ?? undefined,
      q: searchParams.get("q") ?? undefined,
      sort: searchParams.get("sort") ?? "recommended",
    }),
    [searchParams]
  );

  const setFilter = (key: keyof TourFilters, value?: string) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (value) next.set(key, value);
      else next.delete(key);
      return next;
    });
  };

  const { data: tours = [], isLoading } = useTours(filters);
  const { data: destinations = [] } = useDestinations();
  const { data: styles = [] } = useTravelStyles();

  const activeChips = useMemo(() => {
    const chips: Array<{ key: keyof TourFilters; label: string }> = [];
    if (filters.destination) {
      const d = destinations.find((x) => x.slug === filters.destination);
      if (d) chips.push({ key: "destination", label: d.name });
    }
    if (filters.style) {
      const s = styles.find((x) => x.slug === filters.style);
      if (s) chips.push({ key: "style", label: s.title });
    }
    if (filters.q) chips.push({ key: "q", label: `"${filters.q}"` });
    return chips;
  }, [filters, destinations, styles]);

  const toggleDestination = (slug: string) => {
    setFilter("destination", filters.destination === slug ? undefined : slug);
  };

  const toggleStyle = (slug: string) => {
    setFilter("style", filters.style === slug ? undefined : slug);
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

          <div className="filter-group">
            <button>
              <span>Destination</span>
              <Icon name="down" size={17} />
            </button>
            <div className="checks">
              {destinations.map((d) => (
                <label key={d.slug}>
                  <input
                    type="checkbox"
                    checked={filters.destination === d.slug}
                    onChange={() => toggleDestination(d.slug)}
                  />
                  {d.name} <small>{d.journeyCount}</small>
                </label>
              ))}
            </div>
          </div>

          <div className="filter-group">
            <button>
              <span>Travel style</span>
              <Icon name="down" size={17} />
            </button>
            <div className="checks">
              {styles.map((s) => (
                <label key={s.slug}>
                  <input
                    type="checkbox"
                    checked={filters.style === s.slug}
                    onChange={() => toggleStyle(s.slug)}
                  />
                  {s.title}
                </label>
              ))}
            </div>
          </div>

          <Button onClick={() => setShowFilters(false)}>
            Show {tours.length} {tours.length === 1 ? "journey" : "journeys"}
          </Button>
        </aside>

        <section className="results">
          <div className="results-bar">
            <div>
              <strong>
                {tours.length} {tours.length === 1 ? "journey" : "journeys"}
              </strong>
              <div className="chips">
                {activeChips.map((chip) => (
                  <button
                    key={chip.key}
                    onClick={() => setFilter(chip.key, undefined)}
                  >
                    {chip.label} <Icon name="close" size={14} />
                  </button>
                ))}
              </div>
            </div>
            <div>
              <button
                className="filter-mobile"
                onClick={() => setShowFilters(true)}
              >
                <Icon name="sliders" /> Filters
              </button>
              <div style={{ position: "relative" }}>
                <button className="sort" onClick={() => setSortOpen(!sortOpen)}>
                  Sort:{" "}
                  <strong>
                    {
                      SORT_OPTIONS.find(
                        ([k]) => k === (filters.sort ?? "recommended"),
                      )?.[1]
                    }
                  </strong>{" "}
                  <Icon name="down" size={16} />
                </button>
                {sortOpen && (
                  <div
                    style={{
                      position: "absolute",
                      top: "100%",
                      right: 0,
                      marginTop: 6,
                      background: "white",
                      border: "1px solid var(--line)",
                      borderRadius: 8,
                      padding: 6,
                      minWidth: 220,
                      zIndex: 10,
                      boxShadow: "var(--shadow)",
                    }}
                  >
                    {SORT_OPTIONS.map(([key, label]) => (
                      <button
                        key={key}
                        onClick={() => {
                          setFilter("sort", key);
                          setSortOpen(false);
                        }}
                        style={{
                          display: "block",
                          width: "100%",
                          textAlign: "left",
                          padding: "8px 10px",
                          background: "transparent",
                          border: 0,
                          borderRadius: 6,
                          fontSize: 13,
                        }}
                      >
                        {label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          {isLoading ? (
            <div className="tour-grid listing">
              {Array.from({ length: 6 }).map((_, i) => (
                <TourCardSkeleton key={i} />
              ))}
            </div>
          ) : tours.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">
                <Icon name="search" size={32} />
              </div>
              <h3>No journeys found</h3>
              <p>
                We couldn't find any journeys matching your current filters.
              </p>
              <Button variant="secondary" onClick={() => setSearchParams({})}>
                Clear all filters
              </Button>
            </div>
          ) : (
            <div className="tour-grid listing">
              {tours.map((tour) => (
                <TourCard key={tour.slug} tour={tour} />
              ))}
            </div>
          )}
        </section>
      </div>

      <Footer />
    </main>
  );
}