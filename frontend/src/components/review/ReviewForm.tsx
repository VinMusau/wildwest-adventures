import { useState } from "react";
import Icon from "@/components/ui/Icon";
import { useSubmitReview } from "@/features/reviews/useReviews";
import { useToast } from "@/features/toast/useToast";
import { ApiError } from "@/lib/api";

const MIN_BODY = 20;
const MAX_BODY = 2000;
const RATING_LABEL = ["", "Poor", "Fair", "Good", "Very good", "Excellent"];

interface ReviewFormProps {
  bookingReference: string;
  tourTitle: string;
  onDone: () => void;
}

/** The customer's review of a trip they took. Goes to admins for approval. */
export default function ReviewForm({
  bookingReference,
  tourTitle,
  onDone,
}: ReviewFormProps) {
  const [rating, setRating] = useState(0);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [error, setError] = useState<string | null>(null);
  const submit = useSubmitReview(bookingReference);
  const toast = useToast();

  const bodyLength = body.trim().length;
  const canSubmit = rating > 0 && bodyLength >= MIN_BODY && !submit.isPending;

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    submit.mutate(
      { rating, title: title.trim() || undefined, body: body.trim() },
      {
        onSuccess: () => {
          toast.success("Thanks for your review", {
            description: "We'll publish it once our team has checked it.",
          });
          onDone();
        },
        onError: (err) =>
          setError(
            err instanceof ApiError ? err.message : "Could not send review",
          ),
      },
    );
  };

  return (
    <form className="review-form" onSubmit={onSubmit}>
      <h3>Review {tourTitle}</h3>

      <fieldset className="star-picker">
        <legend>Your rating</legend>
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            className={n <= rating ? "star-on" : "star-off"}
            aria-label={`${n} star${n === 1 ? "" : "s"}`}
            aria-pressed={n === rating}
            onClick={() => setRating(n)}
          >
            <Icon name="star" size={22} />
          </button>
        ))}
        <span className="text-muted">{RATING_LABEL[rating]}</span>
      </fieldset>

      <label>
        <span>
          Title <span className="text-muted">(optional)</span>
        </span>
        <input
          value={title}
          maxLength={120}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Sum up your trip in a few words"
        />
      </label>

      <label>
        <span>Your review</span>
        <textarea
          value={body}
          maxLength={MAX_BODY}
          rows={5}
          onChange={(e) => setBody(e.target.value)}
          placeholder="What made the trip? What should other travellers know?"
        />
        <span className="text-muted review-form-count">
          {bodyLength < MIN_BODY
            ? `At least ${MIN_BODY - bodyLength} more characters`
            : `${bodyLength}/${MAX_BODY}`}
        </span>
      </label>

      {error && <p className="form-error">{error}</p>}

      <div className="review-form-actions">
        <button type="button" className="button ghost" onClick={onDone}>
          Cancel
        </button>
        <button type="submit" className="button primary" disabled={!canSubmit}>
          {submit.isPending ? "Sending…" : "Send review"}
        </button>
      </div>
    </form>
  );
}
