import { useMemo, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import { useTour } from "@/features/tours/useTour";
import { useAvailability } from "@/features/booking/useAvailability";
import { useCreateBooking } from "@/features/booking/useCreateBooking";
import { usePaymentInit } from "@/features/booking/usePaymentInit";
import PaystackPayment from "@/features/booking/PaystackPayment";
import { useAuth } from "@/features/auth/AuthContext";
import { formatPrice } from "@/lib/format";
import { ApiError } from "@/lib/api";
import type { Availability, Booking, PaymentInitResponse, TravelerType } from "@/types";
import { useQueryClient } from "@tanstack/react-query";
import { apiPost } from "@/lib/api";
import { useToast } from "@/features/toast/useToast";
import AvailabilitySkeleton from "../features/booking/AvailabilitySkeleton";

const STEPS = ["Dates & travellers", "Your details", "Make it yours", "Review & pay"];

const ADDON_OPTIONS: Array<{ code: string; label: string; description: string; priceCents: number }> = [
  { code: "airport-transfer", label: "Private airport transfer", description: "A seamless welcome from the moment you land.", priceCents: 18000 },
  { code: "travel-cover",     label: "Comprehensive travel cover", description: "Peace of mind for every part of your journey.", priceCents: 32000 },
  { code: "balloon-safari",   label: "Hot-air balloon safari", description: "See the Serengeti come alive from above.", priceCents: 62000 },
];

interface Draft {
  departureDate: string | null;
  travelersCount: number;
  travelerDetails: Array<{ firstName: string; lastName: string; travelerType: TravelerType }>;
  lead: {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    country: string;
  };
  addons: string[];
}

export default function BookingPage() {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchParams] = useSearchParams();

  const urlDate = searchParams.get("date");
  const urlGuests = searchParams.get("guests");
  
  const parsedGuests = Math.max(1, parseInt(urlGuests || "2", 10));

  const { data: tour, isLoading: tourLoading } = useTour(slug);
  const { data: availability = [], isLoading: availLoading } = useAvailability(slug);
  
  const createBooking = useCreateBooking();
  const paymentInit = usePaymentInit();
  const [pendingBooking, setPendingBooking] = useState<Booking | null>(null);
  const [paymentData, setPaymentData] = useState<PaymentInitResponse | null>(null);
  const queryClient = useQueryClient();
  const toast = useToast();

  const [step, setStep] = useState(1);
  
  const [draft, setDraft] = useState<Draft>(() => {
    const initialTravelers = Array.from({ length: parsedGuests }, () => ({
      firstName: "",
      lastName: "",
      travelerType: "ADULT" as TravelerType,
    }));
    
    return {
      departureDate: urlDate || null, 
      travelersCount: parsedGuests,   
      travelerDetails: initialTravelers,
      lead: {
        firstName: user?.firstName ?? "",
        lastName: user?.lastName ?? "",
        email: user?.email ?? "",
        phone: "",
        country: "United States",
      },
      addons: [],
    };
  });

  const [error, setError] = useState<string | null>(null);

  const selectedDate: Availability | undefined = useMemo(
    () => availability.find((a) => a.date === draft.departureDate),
    [availability, draft.departureDate]
  );

  const pricePerPerson = selectedDate?.priceCents ?? tour?.priceCents ?? 0;
  const subtotal = pricePerPerson * draft.travelersCount;
  const addonsTotal = useMemo(
    () =>
      ADDON_OPTIONS
        .filter((a) => draft.addons.includes(a.code))
        .reduce((sum, a) => sum + a.priceCents * draft.travelersCount, 0),
    [draft.addons, draft.travelersCount]
  );
  const total = subtotal + addonsTotal;

  if (tourLoading || !tour) {
    return (
      <main className="booking-page">
        <Nav />
        <div className="booking-layout">
          <p className="text-muted">Loading…</p>
        </div>
      </main>
    );
  }

  const canProceed = (() => {
    if (step === 1) return Boolean(draft.departureDate) && draft.travelersCount > 0;
    if (step === 2) {
      const { firstName, lastName, email } = draft.lead;
      const allTravelersFilled = draft.travelerDetails.every(
        (t) => t.firstName.trim() && t.lastName.trim()
      );
      return Boolean(firstName.trim() && lastName.trim() && email.includes("@") && allTravelersFilled);
    }
    return true;
  })();

  const next = () => {
    setError(null);

    if (step < 4) {
      setStep(step + 1);
      return;
    }

    if (!draft.departureDate || !tour) return;
    if (pendingBooking) return;

    createBooking.mutate(
      {
        tourSlug: tour.slug,
        departureDate: draft.departureDate,
        travelers: draft.travelerDetails,
        lead: draft.lead,
        addons: draft.addons,
      },
      {
        onSuccess: (booking) => {
          setPendingBooking(booking);
          paymentInit.mutate(booking.reference, {
            onSuccess: (data) => setPaymentData(data),
            onError: (err) => {
              toast.error("Payment initialization failed", { description: err.message });
              setError(
                err instanceof ApiError ? err.message : "Could not start payment"
              );
            },
          });
        },
        onError: (err) => {
          toast.error("Booking creation failed", { description: err.message });
          setError(
            err instanceof ApiError ? err.message : "Could not create booking"
          );
        },
      }
    );
  };

  const updateTravelersCount = (delta: number) => {
    setDraft((prev) => {
      const nextCount = Math.max(1, Math.min(8, prev.travelersCount + delta));
      const details = [...prev.travelerDetails];
      while (details.length < nextCount) {
        details.push({ firstName: "", lastName: "", travelerType: "ADULT" });
      }
      while (details.length > nextCount) details.pop();
      return { ...prev, travelersCount: nextCount, travelerDetails: details };
    });
  };

  return (
    <main className="booking-page">
      <Nav />

      <div className="booking-head">
        <Link to={`/journeys/${tour.slug}`}>
          <Icon name="arrow" /> Back to journey
        </Link>
        <span>SECURE BOOKING</span>
      </div>

      <div className="steps">
        {STEPS.map((label, i) => {
          const index = i + 1;
          const state = step === index ? "active" : step > index ? "done" : "";
          return (
            <div className={state} key={label}>
              <span>
                {step > index ? <Icon name="check" size={16} /> : index}
              </span>
              <strong>{label}</strong>
            </div>
          );
        })}
      </div>

      <div className="booking-layout">
        <section className="booking-form">
          {step === 1 && (
            <>
              <span className="eyebrow">Step 1 of 4</span>
              <h1>Choose your departure</h1>

              {tour.tourType === "ON_REQUEST" ? (
                <>
                  <p>Pick any date that works for you. We'll confirm it with our local experts.</p>
                  <div style={{ margin: "24px 0" }}>
                    <label style={{ display: "block", fontSize: 11, fontWeight: 600 }}>
                      Preferred departure date
                      <input
                        type="date"
                        value={draft.departureDate || ""}
                        min={new Date().toISOString().split("T")[0]}
                        onChange={(e) =>
                          setDraft((prev) => ({ ...prev, departureDate: e.target.value }))
                        }
                        style={{
                          display: "block",
                          width: "100%",
                          height: 48,
                          marginTop: 6,
                          padding: "0 12px",
                          border: "1px solid var(--line)",
                          borderRadius: 8,
                        }}
                        required
                      />
                    </label>
                  </div>
                </>
              ) : (
                <>
                  <p>Select an available date for your private journey.</p>
                  {availLoading ? (
                    <AvailabilitySkeleton />
                  ) : availability.length === 0 ? (
                    <p className="text-muted">No departures scheduled yet.</p>
                  ) : (
                    <div className="departure-list">
                      {availability.slice(0, 12).map((a) => {
                        const soldOut = a.remaining === 0;
                        const active = draft.departureDate === a.date;
                        return (
                          <button
                            key={a.id}
                            type="button"
                            disabled={soldOut}
                            className={`departure-card ${active ? "active" : ""} ${soldOut ? "sold-out" : ""}`}
                            onClick={() =>
                              setDraft((prev) => ({
                                ...prev,
                                departureDate: a.date,
                              }))
                            }
                          >
                            <strong>
                              {new Date(a.date).toLocaleDateString("en-US", {
                                weekday: "short",
                                day: "numeric",
                                month: "short",
                              })}
                            </strong>
                            <small>
                              {soldOut ? "Sold out" : `${a.remaining} places left`}
                            </small>
                            <span>{formatPrice(a.priceCents, a.currency)}</span>
                          </button>
                        );
                      })}
                    </div>
                  )}
                </>
              )}

              <div className="traveller-row" style={{ marginTop: 24 }}>
                <div>
                  <strong>Travellers</strong>
                  <span>Ages 12 and over</span>
                </div>
                <div className="stepper">
                  <button
                    type="button"
                    onClick={() => updateTravelersCount(-1)}
                  >
                    −
                  </button>
                  <span>{draft.travelersCount}</span>
                  <button type="button" onClick={() => updateTravelersCount(1)}>
                    +
                  </button>
                </div>
              </div>
            </>
          )}

          {step === 2 && (
            <>
              <span className="eyebrow">Step 2 of 4</span>
              <h1>Tell us about you</h1>
              <p>We'll use these details to prepare your journey.</p>

              <div className="form-grid">
                <label>
                  First name
                  <input
                    value={draft.lead.firstName}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        lead: { ...draft.lead, firstName: e.target.value },
                      })
                    }
                  />
                </label>
                <label>
                  Last name
                  <input
                    value={draft.lead.lastName}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        lead: { ...draft.lead, lastName: e.target.value },
                      })
                    }
                  />
                </label>
                <label className="wide">
                  Email address
                  <input
                    type="email"
                    value={draft.lead.email}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        lead: { ...draft.lead, email: e.target.value },
                      })
                    }
                  />
                </label>
                <label>
                  Phone number
                  <input
                    value={draft.lead.phone}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        lead: { ...draft.lead, phone: e.target.value },
                      })
                    }
                  />
                </label>
                <label>
                  Country
                  <input
                    value={draft.lead.country}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        lead: { ...draft.lead, country: e.target.value },
                      })
                    }
                  />
                </label>
              </div>

              {draft.travelerDetails.map((traveler, i) => (
                <div className="traveller-card" key={i}>
                  <div style={{ flex: 1 }}>
                    <span>TRAVELLER {i + 1}</span>
                    <div className="form-grid" style={{ marginTop: 8 }}>
                      <label>
                        First name
                        <input
                          value={traveler.firstName}
                          onChange={(e) => {
                            const details = [...draft.travelerDetails];
                            details[i] = {
                              ...traveler,
                              firstName: e.target.value,
                            };
                            setDraft({ ...draft, travelerDetails: details });
                          }}
                        />
                      </label>
                      <label>
                        Last name
                        <input
                          value={traveler.lastName}
                          onChange={(e) => {
                            const details = [...draft.travelerDetails];
                            details[i] = {
                              ...traveler,
                              lastName: e.target.value,
                            };
                            setDraft({ ...draft, travelerDetails: details });
                          }}
                        />
                      </label>
                    </div>
                  </div>
                </div>
              ))}
            </>
          )}

          {step === 3 && (
            <>
              <span className="eyebrow">Step 3 of 4</span>
              <h1>Make it yours</h1>
              <p>Optional touches to make your journey even more seamless.</p>
              <div className="addons">
                {ADDON_OPTIONS.map((a) => (
                  <label key={a.code}>
                    <input
                      type="checkbox"
                      checked={draft.addons.includes(a.code)}
                      onChange={() => {
                        setDraft((prev) => ({
                          ...prev,
                          addons: prev.addons.includes(a.code)
                            ? prev.addons.filter((c) => c !== a.code)
                            : [...prev.addons, a.code],
                        }));
                      }}
                    />
                    <div>
                      <strong>{a.label}</strong>
                      <p>{a.description}</p>
                    </div>
                    <span>{formatPrice(a.priceCents, "USD")}</span>
                  </label>
                ))}
              </div>
            </>
          )}

          {step === 4 && (
            <>
              <span className="eyebrow">Step 4 of 4</span>
              <h1>Review & pay</h1>

              {!pendingBooking && (
                <>
                  <p>
                    Your place is held for 20 minutes while you complete
                    payment.
                  </p>
                  <div
                    className="payment"
                    style={{
                      background: "#fff9f5",
                      borderColor: "var(--terracotta)",
                    }}
                  >
                    <strong>Ready to reserve</strong>
                    <p style={{ fontSize: 13, color: "var(--muted)" }}>
                      Click "Reserve my place" to lock in your departure. You'll
                      then choose how to pay.
                    </p>
                  </div>
                  {error && (
                    <p className="form-error" style={{ marginTop: 16 }}>
                      {error}
                    </p>
                  )}
                </>
              )}

              {pendingBooking && !paymentData && (
                <p className="text-muted">Preparing secure payment…</p>
              )}

              {pendingBooking &&
                paymentData?.provider === "PAYSTACK" &&
                paymentData.accessCode && (
                  <PaystackPayment
                    accessCode={paymentData.accessCode}
                    amountLabel={formatPrice(
                      pendingBooking.totalCents,
                      pendingBooking.currency,
                    )}
                    onSuccess={async () => {
                      try {
                        await apiPost(
                          `/v1/bookings/${pendingBooking.reference}/verify-payment`,
                          {},
                        );
                        queryClient.invalidateQueries({
                          queryKey: ["booking", pendingBooking.reference],
                        });
                        queryClient.invalidateQueries({
                          queryKey: ["bookings", "mine"],
                        });
                        toast.success("Booking confirmed", {
                          description: `Reference ${pendingBooking.reference}`,
                        });
                      } catch (e) {
                        console.warn(
                          "Verify failed; webhook will reconcile",
                          e,
                        );
                        toast.info("Payment received", {
                          description: "Finalising your booking…",
                        });
                      }
                      navigate(`/confirmed/${pendingBooking.reference}`, {
                        replace: true,
                      });
                    }}
                    onCancel={() => {
                      setError(
                        "Payment was cancelled. Your place is still held.",
                      );
                    }}
                  />
                )}

              {pendingBooking &&
                paymentData?.provider === "STRIPE" &&
                paymentData.clientKey && (
                  <p className="form-error">
                    Stripe flow not wired in this build. Set
                    PAYMENT_DEFAULT_PROVIDER=PAYSTACK in the backend .env.
                  </p>
                )}
            </>
          )}

          <div className="form-actions">
            {step > 1 && !paymentData && (
              <Button variant="ghost" onClick={() => setStep(step - 1)}>
                Back
              </Button>
            )}
            {!paymentData && (
              <Button
                onClick={next}
                disabled={
                  !canProceed ||
                  createBooking.isPending ||
                  paymentInit.isPending
                }
              >
                {step === 4
                  ? createBooking.isPending || paymentInit.isPending
                    ? "Reserving…"
                    : "Reserve my place"
                  : "Continue"}{" "}
                <Icon name="arrow" />
              </Button>
            )}
          </div>
        </section>

        <aside className="order-summary">
          <img src={tour.imageUrl} alt={tour.title} />
          <span className="eyebrow">Your journey</span>
          <h3>{tour.title}</h3>
          <p>
            {tour.place} · {tour.durationDays} days
          </p>
          <hr />
          <dl>
            <div>
              <dt>Departure</dt>
              <dd>
                {draft.departureDate
                  ? new Date(draft.departureDate).toLocaleDateString("en-US", {
                      day: "numeric",
                      month: "long",
                      year: "numeric",
                    })
                  : "—"}
              </dd>
            </div>
            <div>
              <dt>Travellers</dt>
              <dd>
                {draft.travelersCount}{" "}
                {draft.travelersCount === 1 ? "adult" : "adults"}
              </dd>
            </div>
            <div>
              <dt>Journey</dt>
              <dd>
                {formatPrice(subtotal, selectedDate?.currency ?? tour.currency)}
              </dd>
            </div>
            {draft.addons.map((code) => {
              const addon = ADDON_OPTIONS.find((a) => a.code === code);
              if (!addon) return null;
              return (
                <div key={code}>
                  <dt>{addon.label}</dt>
                  <dd>
                    {formatPrice(
                      addon.priceCents * draft.travelersCount,
                      "USD",
                    )}
                  </dd>
                </div>
              );
            })}
          </dl>
          <hr />
          <div className="total">
            <span>Total</span>
            <strong>
              {formatPrice(total, selectedDate?.currency ?? tour.currency)}{" "}
              <small>{selectedDate?.currency ?? tour.currency}</small>
            </strong>
          </div>
          <p className="secure">
            <Icon name="shield" /> No payment collected today
          </p>
        </aside>
      </div>
    </main>
  );
}