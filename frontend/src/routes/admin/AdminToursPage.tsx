import { Link } from "react-router";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import { useAdminTours, useDeleteTour } from "@/features/admin/useAdminTours";
import { formatPrice } from "@/lib/format";
import { useToast } from "@/features/toast/useToast";
import { useConfirm } from "@/features/confirm/useConfirm";
import AdminTableSkeleton from "@/components/admin/AdminTableSkeleton";

export default function AdminToursPage() {
  const { data: tours = [], isLoading } = useAdminTours();
  const deleteTour = useDeleteTour();
  const toast = useToast();
  const confirm = useConfirm();

  const handleDelete = async (slug: string, title: string) => {
    const ok = await confirm({
      title: `Delete "${title}"?`,
      description:
        "This will permanently remove the tour and all associated itinerary data. This action cannot be undone.",
      confirmLabel: "Delete tour",
      variant: "danger",
    });

    if (!ok) return;

    deleteTour.mutate(slug, {
      onSuccess: () => toast.success("Tour deleted", { description: title }),
      onError: () => toast.error("Could not delete tour"),
    });
  };

  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Content</span>
          <h1>Tours</h1>
        </div>
        <Link to="/admin/tours/new" className="button primary">
          New tour <Icon name="arrow" />
        </Link>
      </div>

      {isLoading ? (
        <AdminTableSkeleton rows={8} cols={7} />
      ) : tours.length === 0 ? (
        <p className="text-muted">No tours yet. Create your first one.</p>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Order</th>
                <th>Title</th>
                <th>Destination</th>
                <th>Duration</th>
                <th>Price</th>
                <th>Featured</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {tours.map((tour) => (
                <tr key={tour.slug}>
                  <td data-label="Order" className="admin-cell-num">
                    {tour.displayOrder}
                  </td>
                  <td data-label="Title">
                    <Link
                      to={`/admin/tours/${tour.slug}/edit`}
                      className="admin-link"
                    >
                      {tour.title}
                    </Link>
                    <small className="admin-slug">{tour.slug}</small>
                  </td>
                  <td data-label="Destination">{tour.destinationName}</td>
                  <td data-label="Duration">{tour.durationDays} days</td>
                  <td data-label="Price">
                    {formatPrice(tour.priceCents, tour.currency)}
                  </td>
                  <td data-label="Featured">
                    {tour.featured ? (
                      <span className="badge-yes">Yes</span>
                    ) : (
                      <span className="badge-no">—</span>
                    )}
                  </td>
                  <td className="admin-cell-actions">
                    <Link to={`/admin/tours/${tour.slug}/edit`}>Edit</Link>
                    <button
                      className="danger"
                      onClick={() => handleDelete(tour.slug, tour.title)}
                      disabled={deleteTour.isPending}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}