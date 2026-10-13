import { useState } from "react";
import { Link } from "react-router";
import Icon from "@/components/ui/Icon";
import Rating from "@/components/ui/Rating";
import type { Tour } from "@/types";

interface TourCardProps {
  tour: Tour;
}

export default function TourCard({ tour }: TourCardProps) {
  const [saved, setSaved] = useState(false);

  return (
    <article className="tour-card">
      <div className="tour-image">
        <img src={tour.image} alt={tour.title} />
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
          <Rating value={tour.rating} reviews={tour.reviews} />
        </div>
        <h3>{tour.title}</h3>
        <div className="tour-meta">
          <span>
            <Icon name="clock" size={17} /> {tour.days}
          </span>
          <span>
            From <strong>{tour.price}</strong>
          </span>
        </div>
        <Link to={`/journeys/${tour.slug}`} className="text-link">
          Explore this journey <Icon name="arrow" size={18} />
        </Link>
      </div>
    </article>
  );
}