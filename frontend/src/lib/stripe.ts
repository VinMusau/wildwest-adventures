import { loadStripe } from "@stripe/stripe-js";

const key = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

if (!key) {
  console.warn(
    "VITE_STRIPE_PUBLISHABLE_KEY is not set. Payment step will not work."
  );
}

export const stripePromise = key ? loadStripe(key) : null;