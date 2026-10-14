import { useState } from "react";
import { Link } from "react-router";
import Icon from "@/components/ui/Icon";
import Rating from "@/components/ui/Rating";
import { formatPrice } from "@/lib/format";
import type { Tour } from "@/types";

interface TourCardProps {
  tour: Tour;
}

export default function TourCard({ tour }: TourCardProps) {
  const [saved, setSaved] = useState(false);

  return (
    <article className="tour-card">
      <div className="tour-image">
        <img src={tour.imageUrl} alt={tour.title} />
        <span className="tag">{tour.tag}</span>
        <button
          className={`heart ${saved ? "saved" : ""}`}
          onClick={() => setSaved(!saved)}
          aria-label="Save tour"
        >
          <Icon name="heart" />
        </button>
      </div>
      <div className="tour-body">
        <div className="eyebrow-row">
          <span>{tour.place}</span>
          <Rating value={tour.rating.toFixed(1)} reviews={tour.reviewCount} />
        </div>
        <h3>{tour.title}</h3>
        <div className="tour-meta">
          <span>
            <Icon name="clock" size={17} /> {tour.durationDays} days
          </span>
          <span>
            From <strong>{formatPrice(tour.priceCents, tour.currency)}</strong>
          </span>
        </div>
        <Link to={`/journeys/${tour.slug}`} className="text-link">
          Explore this journey <Icon name="arrow" size={18} />
        </Link>
      </div>
    </article>
  );
}