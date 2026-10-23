import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import { useTour } from "@/features/tours/useTour";
import { useDestinations } from "@/features/destinations/useDestinations";
import { useTravelStyles } from "@/features/travel-styles/useTravelStyles";
import {
  useAdminTour,
  useCreateTour,
  useUpdateTour,
} from "@/features/admin/useAdminTours";
import { ApiError } from "@/lib/api";
import type { AdminItineraryDay, TourWriteInput } from "@/types";
import { useToast } from "@/features/toast/useToast";
import ImageUploader from "@/components/admin/ImageUploader";

const EMPTY: TourWriteInput = {
  title: "",
  slug: "",
  place: "",
  destinationSlug: "",
  durationDays: 7,
  priceCents: 500000,
  currency: "USD",
  rating: 5.0,
  reviewCount: 0,
  tag: "New",
  imageUrl: "",
  featured: false,
  displayOrder: 100,
  styleSlugs: [],
  itinerary: [],
  inclusions: [],
};

export default function AdminTourEditPage() {
  const { slug } = useParams<{ slug: string }>();
  const isNew = slug === "new" || !slug;
  const navigate = useNavigate();
  const toast = useToast();

  const { data: existing, isLoading: loadingExisting } = useAdminTour(isNew ? undefined : slug);
  const { data: destinations = [] } = useDestinations();
  const { data: styles = [] } = useTravelStyles();

  const create = useCreateTour();
  const update = useUpdateTour(slug ?? "");
  const mutation = isNew ? create : update;

  const [form, setForm] = useState<TourWriteInput>(EMPTY);
  const [error, setError] = useState<string | null>(null);
  const [priceUsd, setPriceUsd] = useState<string>("5000");

  useEffect(() => {
    if (existing) {
      setForm({
        title: existing.title,
        slug: existing.slug,
        place: existing.place,
        destinationSlug: existing.destinationSlug,
        durationDays: existing.durationDays,
        priceCents: existing.priceCents,
        currency: existing.currency,
        rating: existing.rating,
        reviewCount: existing.reviewCount,
        tag: existing.tag,
        imageUrl: existing.imageUrl,
        featured: existing.featured,
        displayOrder: existing.displayOrder,
        styleSlugs: existing.styleSlugs,
        itinerary: existing.itinerary.map((d) => ({
          dayNumber: d.dayNumber,
          title: d.title,
          description: d.description,
          accommodation: d.accommodation ?? "",
          mealsIncluded: d.mealsIncluded ?? "",
        })),
        inclusions: existing.inclusions,
      });
      setPriceUsd((existing.priceCents / 100).toString());
    }
  }, [existing]);

  const updateField = <K extends keyof TourWriteInput>(
    key: K,
    value: TourWriteInput[K]
  ) => setForm((f) => ({ ...f, [key]: value }));

  const toggleStyle = (styleSlug: string) => {
    setForm((f) => ({
      ...f,
      styleSlugs: f.styleSlugs.includes(styleSlug)
        ? f.styleSlugs.filter((s) => s !== styleSlug)
        : [...f.styleSlugs, styleSlug],
    }));
  };

  const addDay = () => {
    setForm((f) => ({
      ...f,
      itinerary: [
        ...f.itinerary,
        {
          dayNumber: f.itinerary.length + 1,
          title: "",
          description: "",
          accommodation: "",
          mealsIncluded: "",
        },
      ],
    }));
  };

  const updateDay = (index: number, patch: Partial<AdminItineraryDay>) => {
    setForm((f) => {
      const days = [...f.itinerary];
      days[index] = { ...days[index], ...patch };
      return { ...f, itinerary: days };
    });
  };

  const removeDay = (index: number) => {
    setForm((f) => {
      const days = f.itinerary.filter((_, i) => i !== index);
      // Re-number
      return { ...f, itinerary: days.map((d, i) => ({ ...d, dayNumber: i + 1 })) };
    });
  };

  const addInclusion = () => {
    setForm((f) => ({ ...f, inclusions: [...f.inclusions, ""] }));
  };

  const updateInclusion = (index: number, value: string) => {
    setForm((f) => {
      const list = [...f.inclusions];
      list[index] = value;
      return { ...f, inclusions: list };
    });
  };

  const removeInclusion = (index: number) => {
    setForm((f) => ({
      ...f,
      inclusions: f.inclusions.filter((_, i) => i !== index),
    }));
  };

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const payload: TourWriteInput = {
      ...form,
      priceCents: Math.round(parseFloat(priceUsd || "0") * 100),
      inclusions: form.inclusions.filter((s) => s.trim()),
    };

    mutation.mutate(payload, {
      onSuccess: (saved) => {
        toast.success(`Tour ${isNew ? "created" : "updated"}`, {
          description: saved.title,
        });
        navigate(`/admin/tours/${saved.slug}/edit`);
      },
      onError: (err) => {
        const message = err instanceof ApiError ? err.message : "Could not save tour";
        setError(message);
        toast.error("Could not save tour", { description: message });
      },
    });
  };

  if (!isNew && loadingExisting) {
    return <div className="admin-page"><p className="text-muted">Loading…</p></div>;
  }

  return (
    <form className="admin-page" onSubmit={submit}>
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">{isNew ? "Create" : "Edit"}</span>
          <h1>{isNew ? "New tour" : form.title || "Tour"}</h1>
        </div>
        <div style={{ display: "flex", gap: 10 }}>
          <Link to="/admin/tours" className="button secondary">
            Cancel
          </Link>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? "Saving…" : "Save"} <Icon name="arrow" />
          </Button>
        </div>
      </div>

      {error && <p className="form-error">{error}</p>}

      <section className="admin-form-section">
        <h2>Basics</h2>
        <div className="admin-form-grid">
          <label className="wide">
            Title
            <input
              value={form.title}
              onChange={(e) => updateField("title", e.target.value)}
              required
            />
          </label>
          <label className="wide">
            Slug (leave blank to auto-generate)
            <input
              value={form.slug ?? ""}
              onChange={(e) => updateField("slug", e.target.value)}
              placeholder="my-tour-slug"
            />
          </label>
          <label>
            Place
            <input
              value={form.place}
              onChange={(e) => updateField("place", e.target.value)}
              required
            />
          </label>
          <label>
            Destination
            <select
              value={form.destinationSlug}
              onChange={(e) => updateField("destinationSlug", e.target.value)}
              required
            >
              <option value="">Choose…</option>
              {destinations.map((d) => (
                <option key={d.slug} value={d.slug}>
                  {d.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Duration (days)
            <input
              type="number"
              min={1}
              value={form.durationDays}
              onChange={(e) =>
                updateField("durationDays", parseInt(e.target.value || "0"))
              }
              required
            />
          </label>
          <label>
            Price (USD)
            <input
              type="number"
              step="0.01"
              min={0}
              value={priceUsd}
              onChange={(e) => setPriceUsd(e.target.value)}
              required
            />
          </label>
          <label>
            Tag
            <input
              value={form.tag}
              onChange={(e) => updateField("tag", e.target.value)}
              required
            />
          </label>
          <label>
            Display order
            <input
              type="number"
              min={0}
              value={form.displayOrder}
              onChange={(e) =>
                updateField("displayOrder", parseInt(e.target.value || "0"))
              }
              required
            />
          </label>
          <div className="wide">
            <label
              style={{
                display: "block",
                fontSize: "11px",
                fontWeight: 600,
                marginBottom: "6px",
              }}
            >
              Cover Image
            </label>
            <ImageUploader
              initialImage={form.imageUrl}
              onUploadSuccess={(url) => updateField("imageUrl", url)}
            />
            {/* Hidden input to satisfy HTML5 'required' validation if needed */}
            <input
              type="text"
              value={form.imageUrl}
              required
              style={{
                opacity: 0,
                height: 0,
                position: "absolute",
                pointerEvents: "none",
              }}
              tabIndex={-1}
            />
          </div>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={form.featured}
              onChange={(e) => updateField("featured", e.target.checked)}
            />
            Featured on homepage
          </label>
        </div>
      </section>

      <section className="admin-form-section">
        <h2>Travel styles</h2>
        <div className="admin-chips">
          {styles.map((s) => (
            <button
              type="button"
              key={s.slug}
              className={`chip ${form.styleSlugs.includes(s.slug) ? "active" : ""}`}
              onClick={() => toggleStyle(s.slug)}
            >
              {s.title}
            </button>
          ))}
        </div>
      </section>

      <section className="admin-form-section">
        <div className="admin-section-head">
          <h2>Itinerary</h2>
          <Button type="button" variant="ghost" onClick={addDay}>
            Add day <Icon name="arrow" />
          </Button>
        </div>
        {form.itinerary.length === 0 && (
          <p className="text-muted">No days yet. Add the first one.</p>
        )}
        {form.itinerary.map((day, i) => (
          <div className="admin-itinerary-day" key={i}>
            <div className="admin-itinerary-head">
              <strong>Day {day.dayNumber}</strong>
              <button
                type="button"
                className="nav-link-btn danger"
                onClick={() => removeDay(i)}
              >
                Remove
              </button>
            </div>
            <div className="admin-form-grid">
              <label className="wide">
                Title
                <input
                  value={day.title}
                  onChange={(e) => updateDay(i, { title: e.target.value })}
                />
              </label>
              <label className="wide">
                Description
                <textarea
                  rows={3}
                  value={day.description}
                  onChange={(e) =>
                    updateDay(i, { description: e.target.value })
                  }
                />
              </label>
              <label>
                Accommodation
                <input
                  value={day.accommodation ?? ""}
                  onChange={(e) =>
                    updateDay(i, { accommodation: e.target.value })
                  }
                />
              </label>
              <label>
                Meals included
                <input
                  value={day.mealsIncluded ?? ""}
                  onChange={(e) =>
                    updateDay(i, { mealsIncluded: e.target.value })
                  }
                />
              </label>
            </div>
          </div>
        ))}
      </section>

      <section className="admin-form-section">
        <div className="admin-section-head">
          <h2>Inclusions</h2>
          <Button type="button" variant="ghost" onClick={addInclusion}>
            Add inclusion <Icon name="arrow" />
          </Button>
        </div>
        {form.inclusions.length === 0 && (
          <p className="text-muted">Nothing included yet.</p>
        )}
        {form.inclusions.map((label, i) => (
          <div className="admin-inclusion-row" key={i}>
            <input
              value={label}
              onChange={(e) => updateInclusion(i, e.target.value)}
              placeholder="What's included…"
            />
            <button
              type="button"
              className="nav-link-btn danger"
              onClick={() => removeInclusion(i)}
            >
              Remove
            </button>
          </div>
        ))}
      </section>

      <div className="admin-form-actions">
        <Link to="/admin/tours" className="button secondary">
          Cancel
        </Link>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Saving…" : "Save tour"} <Icon name="arrow" />
        </Button>
      </div>
    </form>
  );
}