import { tokenStorage, AUTH_EXPIRED_EVENT } from "@/features/auth/storage";

const BASE = "/api";

interface ApiErrorShape {
  status: number;
  message: string;
}

export class ApiError extends Error {
  status: number;
  constructor({ status, message }: ApiErrorShape) {
    super(message);
    this.status = status;
    this.name = "ApiError";
  }
}

async function request<T>(
  path: string,
  init: RequestInit = {}
): Promise<T> {
  const token = tokenStorage.get();
  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(init.headers as Record<string, string> | undefined),
  };
  if (init.body && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE}${path}`, { ...init, headers });

  if (res.status === 401) {
    tokenStorage.clear();
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    throw new ApiError({ status: 401, message: "Session expired" });
  }

  if (!res.ok) {
    let message = `API ${res.status}`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
      else if (body?.detail) message = body.detail;
    } catch {
      // ignore
    }
    throw new ApiError({ status: res.status, message });
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

export function apiGet<T>(path: string): Promise<T> {
  return request<T>(path);
}

export function apiPost<T>(path: string, body: unknown): Promise<T> {
  return request<T>(path, { method: "POST", body: JSON.stringify(body) });
}