import Icon from "./Icon";

interface RatingProps {
  value?: string;
  reviews?: number;
}

export default function Rating({ value = "4.9", reviews }: RatingProps) {
  return (
    <span className="rating">
      <Icon name="star" size={15} />
      <strong>{value}</strong>
      {reviews !== undefined && <span>({reviews})</span>}
    </span>
  );
}