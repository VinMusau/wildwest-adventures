import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { AdminCustomersPage } from "@/types";

export function useAdminCustomers(page = 0, size = 25) {
  return useQuery({
    queryKey: ["admin", "customers", page, size],
    queryFn: () =>
      apiGet<AdminCustomersPage>(
        `/v1/admin/customers?page=${page}&size=${size}`
      ),
    placeholderData: (prev) => prev,
  });
}