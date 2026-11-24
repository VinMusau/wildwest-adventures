import type { Review } from "@/types";
import Stars from "./Stars";

export function travelledLabel(date: string) {
  return new Date(date).toLocaleDateString("en-US", {
    month: "long",
    year: "numeric",
  });
}

export default function ReviewCard({ review }: { review: Review }) {
  return (
    <article className="review-card">
      <Stars rating={review.rating} />
      {review.title && <h3>{review.title}</h3>}
      <p>{review.body}</p>
      <div className="review-meta">
        <strong>{review.authorName}</strong>
        {review.authorCountry && <span> · {review.authorCountry}</span>}
        <span> · Travelled {travelledLabel(review.travelledOn)}</span>
        <span className="verified-badge">Verified traveller</span>
      </div>
    </article>
  );
}
