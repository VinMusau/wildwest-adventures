import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { AdminDashboard } from "@/types";

export function useAdminDashboard() {
  return useQuery({
    queryKey: ["admin", "dashboard"],
    queryFn: () => apiGet<AdminDashboard>("/v1/admin/dashboard"),
    staleTime: 60_000,
  });
}