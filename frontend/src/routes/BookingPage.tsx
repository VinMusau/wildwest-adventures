import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import { photos } from "@/data/mock";

const STEPS = ["Dates & travellers", "Your details", "Make it yours", "Review & pay"];

const ADDONS: Array<[string, string, string]> = [
  ["Private airport transfer", "A seamless welcome from the moment you land.", "$180"],
  ["Comprehensive travel cover", "Peace of mind for every part of your journey.", "$320"],
  ["Hot-air balloon safari", "See the Serengeti come alive from above.", "$620"],
];

export default function BookingPage() {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);

  const tour = tours.find((t) => t.slug === slug) ?? tours[0];

  const next = () => {
    if (step === 4) {
      navigate("/confirmed/RW-28461");
    } else {
      setStep(step + 1);
    }
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
              <span>{step > index ? <Icon name="check" size={16} /> : index}</span>
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
              <p>Select an available date for your private journey.</p>

              <div className="calendar">
                <div className="calendar-head">
                  <button>‹</button>
                  <strong>September 2026</strong>
                  <button>›</button>
                </div>
                <div className="week">
                  {["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"].map((d) => (
                    <span key={d}>{d}</span>
                  ))}
                  {Array.from({ length: 35 }, (_, i) => (
                    <button
                      key={i}
                      className={
                        [10, 17, 24].includes(i) ? "available" : i < 3 ? "muted" : ""
                      }
                    >
                      {i < 3 ? 29 + i : i - 2}
                    </button>
                  ))}
                </div>
                <div className="calendar-key">
                  <span>
                    <i /> Available
                  </span>
                  <span>
                    <i /> Selected
                  </span>
                  <span>
                    <i /> Unavailable
                  </span>
                </div>
              </div>

              <div className="traveller-row">
                <div>
                  <strong>Travellers</strong>
                  <span>Ages 12 and over</span>
                </div>
                <div className="stepper">
                  <button>−</button>
                  <span>2</span>
                  <button>+</button>
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
                  <input placeholder="First name" />
                </label>
                <label>
                  Last name
                  <input placeholder="Last name" />
                </label>
                <label className="wide">
                  Email address
                  <input type="email" placeholder="you@email.com" />
                </label>
                <label>
                  Phone number
                  <input placeholder="+1 555 000 0000" />
                </label>
                <label>
                  Country
                  <select>
                    <option>United States</option>
                    <option>United Kingdom</option>
                  </select>
                </label>
              </div>
              <div className="traveller-card">
                <span>TRAVELLER 2</span>
                <strong>Guest details</strong>
                <button>
                  Add details <Icon name="arrow" />
                </button>
              </div>
            </>
          )}

          {step === 3 && (
            <>
              <span className="eyebrow">Step 3 of 4</span>
              <h1>Make it yours</h1>
              <p>Optional touches to make your journey even more seamless.</p>
              <div className="addons">
                {ADDONS.map(([title, description, price], i) => (
                  <label key={title}>
                    <input type="checkbox" defaultChecked={i === 2} />
                    <div>
                      <strong>{title}</strong>
                      <p>{description}</p>
                    </div>
                    <span>{price}</span>
                  </label>
                ))}
              </div>
            </>
          )}

          {step === 4 && (
            <>
              <span className="eyebrow">Step 4 of 4</span>
              <h1>Review & pay</h1>
              <p>Your place is held for 20 minutes while you complete payment.</p>
              <div className="payment">
                <strong>Payment details</strong>
                <label>
                  Card number
                  <input placeholder="1234 5678 9012 3456" />
                </label>
                <div>
                  <label>
                    Expiry
                    <input placeholder="MM / YY" />
                  </label>
                  <label>
                    Security code
                    <input placeholder="CVC" />
                  </label>
                </div>
              </div>
              <label className="terms">
                <input type="checkbox" /> I agree to the booking conditions and
                cancellation policy.
              </label>
            </>
          )}

          <div className="form-actions">
            {step > 1 && (
              <Button variant="ghost" onClick={() => setStep(step - 1)}>
                Back
              </Button>
            )}
            <Button onClick={next}>
              {step === 4 ? "Pay securely" : "Continue"} <Icon name="arrow" />
            </Button>
          </div>
        </section>

        <aside className="order-summary">
          <img src={photos.hero} alt={tour.title} />
          <span className="eyebrow">Your journey</span>
          <h3>{tour.title}</h3>
          <p>
            {tour.place} · {tour.days}
          </p>
          <hr />
          <dl>
            <div>
              <dt>Departure</dt>
              <dd>12 September 2026</dd>
            </div>
            <div>
              <dt>Travellers</dt>
              <dd>2 adults</dd>
            </div>
            <div>
              <dt>Journey</dt>
              <dd>$19,700</dd>
            </div>
            <div>
              <dt>Balloon safari</dt>
              <dd>$1,240</dd>
            </div>
          </dl>
          <hr />
          <div className="total">
            <span>Total</span>
            <strong>
              $20,940 <small>USD</small>
            </strong>
          </div>
          <p className="secure">
            <Icon name="shield" /> Secure, encrypted checkout
          </p>
        </aside>
      </div>
    </main>
  );
}