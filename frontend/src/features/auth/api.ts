import { apiGet, apiPost } from "@/lib/api";
import type { AuthResponse, User } from "@/types";

export interface LoginInput {
  email: string;
  password: string;
}

export interface RegisterInput {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export const authApi = {
  login: (input: LoginInput) => apiPost<AuthResponse>("/v1/auth/login", input),
  register: (input: RegisterInput) => apiPost<AuthResponse>("/v1/auth/register", input),
  me: () => apiGet<User>("/v1/auth/me"),
};