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
import HealthBadge from "@/components/dev/HealthBadge";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: 1,
    },
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
    document.title = "Wild West — Private Journeys";
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ScrollToTop />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/journeys" element={<ToursPage />} />
          <Route path="/journeys/:slug" element={<TourDetailPage />} />
          <Route path="/book/:slug" element={<BookingPage />} />
          <Route path="/confirmed/:reference" element={<ConfirmationPage />} />
        </Routes>
        <HealthBadge />
      </BrowserRouter>
    </QueryClientProvider>
  );
}