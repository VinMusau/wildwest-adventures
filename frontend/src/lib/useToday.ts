import { useState } from "react";

/** Today's date as YYYY-MM-DD (UTC), e.g. for a date input's `min`. */
export function todayIso(): string {
  return new Date().toISOString().split("T")[0];
}

/**
 * Today's date, read once when the component mounts so every render agrees
 * (calling `new Date()` during render makes renders impure).
 */
export function useToday(): string {
  const [today] = useState(todayIso);
  return today;
}
