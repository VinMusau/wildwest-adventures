import { describe, expect, it } from "vitest";
import { formatPrice } from "./format";

describe("formatPrice", () => {
  it("formats cents as whole currency units", () => {
    expect(formatPrice(842000, "USD")).toBe("$8,420");
  });

  it("rounds to whole units", () => {
    expect(formatPrice(1999, "USD")).toBe("$20");
  });

  it("uses the booking's currency", () => {
    // Intl separates the code with a non-breaking space.
    expect(formatPrice(150000, "KES")).toMatch(/^KES\s1,500$/);
  });
});
