import Icon from "./Icon";

interface RatingProps {
  /** Average rating, e.g. "4.8". */
  value: string;
  /** Number of reviews behind the average, shown in brackets. */
  reviews?: number;
}

/** A star and an average rating. Only render it for real, approved reviews. */
export default function Rating({ value, reviews }: RatingProps) {
  return (
    <span className="rating">
      <Icon name="star" size={15} />
      <strong>{value}</strong>
      {reviews !== undefined && <span>({reviews})</span>}
    </span>
  );
}
