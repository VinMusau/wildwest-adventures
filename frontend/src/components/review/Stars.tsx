import Icon from "@/components/ui/Icon";

/** 1 to 5 stars; the unearned ones are dimmed. */
export default function Stars({ rating }: { rating: number }) {
  return (
    <span className="stars" aria-label={`${rating} out of 5 stars`} role="img">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= rating ? "star-on" : "star-off"}>
          <Icon name="star" size={15} />
        </span>
      ))}
    </span>
  );
}
