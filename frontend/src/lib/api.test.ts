import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { apiGet, apiPost, apiUpload, ApiError } from "./api";
import { AUTH_EXPIRED_EVENT, tokenStorage } from "@/features/auth/storage";

function respond(status: number, body?: unknown) {
  return new Response(body === undefined ? null : JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("api client", () => {
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  const sentHeaders = () =>
    fetchMock.mock.calls[0][1].headers as Record<string, string>;

  it("sends JSON with the stored token", async () => {
    tokenStorage.set("abc");
    fetchMock.mockResolvedValue(respond(200, { ok: true }));

    await expect(apiPost("/v1/things", { a: 1 })).resolves.toEqual({
      ok: true,
    });

    expect(fetchMock.mock.calls[0][0]).toBe("/api/v1/things");
    expect(sentHeaders()["Content-Type"]).toBe("application/json");
    expect(sentHeaders()["Authorization"]).toBe("Bearer abc");
  });

  it("lets the browser set the multipart Content-Type for uploads", async () => {
    fetchMock.mockResolvedValue(respond(200, { url: "https://img" }));

    await apiUpload("/v1/admin/tours/upload-image", new FormData());

    expect(sentHeaders()["Content-Type"]).toBeUndefined();
  });

  it("shows the server's problem detail on errors", async () => {
    fetchMock.mockResolvedValue(
      respond(400, { status: 400, detail: "title must not be blank" }),
    );

    const error = await apiGet("/v1/x").catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 400,
      message: "title must not be blank",
    });
  });

  it("treats a 401 with a token as an expired session", async () => {
    tokenStorage.set("expired");
    const expired = vi.fn();
    window.addEventListener(AUTH_EXPIRED_EVENT, expired);
    fetchMock.mockResolvedValue(respond(401, { detail: "Unauthorized" }));

    await expect(apiGet("/v1/bookings/mine")).rejects.toMatchObject({
      status: 401,
      message: "Session expired",
    });
    expect(tokenStorage.get()).toBeNull();
    expect(expired).toHaveBeenCalledOnce();
    window.removeEventListener(AUTH_EXPIRED_EVENT, expired);
  });

  it("shows the server's message for a 401 without a token (wrong password)", async () => {
    const expired = vi.fn();
    window.addEventListener(AUTH_EXPIRED_EVENT, expired);
    fetchMock.mockResolvedValue(
      respond(401, { status: 401, detail: "Invalid credentials" }),
    );

    await expect(
      apiPost("/v1/auth/login", { email: "a@b.co", password: "x" }),
    ).rejects.toMatchObject({ status: 401, message: "Invalid credentials" });
    expect(expired).not.toHaveBeenCalled();
    window.removeEventListener(AUTH_EXPIRED_EVENT, expired);
  });

  it("returns undefined for 204 No Content", async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }));
    await expect(apiGet("/v1/x")).resolves.toBeUndefined();
  });
});
