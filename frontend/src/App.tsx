import { useEffect } from "react";
import {
  BrowserRouter,
  Route,
  Routes,
  useLocation,
} from "react-router";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import HomePage from "@/routes/HomePage";
import ToursPage from "@/routes/ToursPage";
import TourDetailPage from "@/routes/TourDetailPage";
import BookingPage from "@/routes/BookingPage";
import ConfirmationPage from "@/routes/ConfirmationPage";
import LoginPage from "@/routes/LoginPage";
import RegisterPage from "@/routes/RegisterPage";
import HealthBadge from "@/components/dev/HealthBadge";
import { AuthProvider } from "@/features/auth/AuthContext";
import ProtectedRoute from "@/features/auth/ProtectedRoute";
import MyBookingsPage from "@/routes/MyBookingsPage";
import PaymentReturnPage from "@/routes/PaymentReturnPage";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000, retry: 1 },
  },
});

function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" as ScrollBehavior });
  }, [pathname]);
  return null;
}

export default function App() {
  useEffect(() => {
    document.title = "Wild West Adventures";
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <ScrollToTop />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/journeys" element={<ToursPage />} />
            <Route path="/journeys/:slug" element={<TourDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/book/:slug"
              element={
                <ProtectedRoute>
                  <BookingPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/confirmed/:reference"
              element={
                <ProtectedRoute>
                  <ConfirmationPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/account/bookings"
              element={
                <ProtectedRoute>
                  <MyBookingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/payment/return"
              element={
                <ProtectedRoute>
                  <PaymentReturnPage />
                </ProtectedRoute>
              }
            />
          </Routes>
          <HealthBadge />
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}