import { useState } from "react";
import Button from "@/components/ui/Button";
import { useAdminCustomers } from "@/features/admin/useAdminCustomers";
import { formatPrice } from "@/lib/format";

export default function AdminCustomersPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useAdminCustomers(page);

  const customers = data?.content ?? [];
  const total = data?.totalElements ?? 0;

  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Directory</span>
          <h1>Customers</h1>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            {total} {total === 1 ? "customer" : "customers"}
          </p>
        </div>
      </div>

      {isLoading ? (
        <p className="text-muted">Loading customers…</p>
      ) : customers.length === 0 ? (
        <p className="text-muted">No customers yet.</p>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Signed up</th>
                <th>Bookings</th>
                <th>Total spent</th>
                <th>Last booking</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((c) => (
                <tr key={c.id}>
                  <td>
                    {c.firstName} {c.lastName}
                    {c.role === "ADMIN" && (
                      <small className="admin-slug" style={{ color: "var(--terracotta)" }}>
                        ADMIN
                      </small>
                    )}
                  </td>
                  <td>{c.email}</td>
                  <td>
                    {new Date(c.createdAt).toLocaleDateString("en-US", {
                      day: "numeric",
                      month: "short",
                      year: "numeric",
                    })}
                  </td>
                  <td>{c.bookingsCount}</td>
                  <td>{formatPrice(c.totalSpentCents, "USD")}</td>
                  <td>
                    {c.lastBookingAt
                      ? new Date(c.lastBookingAt).toLocaleDateString("en-US", {
                          day: "numeric",
                          month: "short",
                          year: "numeric",
                        })
                      : "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="admin-pagination">
          <Button
            variant="ghost"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            Previous
          </Button>
          <span>
            Page {page + 1} of {data.totalPages}
          </span>
          <Button
            variant="ghost"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage(page + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
}