export interface Tour {
  id: string;
  slug: string;
  title: string;
  place: string;
  destinationSlug: string;
  destinationName: string;
  durationDays: number;
  priceCents: number;
  currency: string;
  rating: number;
  reviewCount: number;
  tag: string;
  imageUrl: string;
  featured: boolean;
  styleSlugs: string[];
}

export interface TravelStyle {
  id: string;
  slug: string;
  title: string;
  description: string;
  imageUrl: string;
  displayOrder: number;
}

export interface Destination {
  id: string;
  slug: string;
  name: string;
  imageUrl: string;
  journeyCount: number;
  displayOrder: number;
}

export interface Health {
  status: string;
  service: string;
  timestamp: string;
}

export interface ItineraryDay {
  dayNumber: number;
  title: string;
  description: string;
  accommodation: string | null;
  mealsIncluded: string | null;
}

export interface TourImage {
  imageUrl: string;
  altText: string | null;
}

export interface TourDetail extends Tour {
  itinerary: ItineraryDay[];
  inclusions: string[];
  images: TourImage[];
}

export type UserRole = "USER" | "ADMIN";

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Availability {
  id: string;
  date: string;              // ISO date
  capacity: number;
  booked: number;
  remaining: number;
  priceCents: number;
  currency: string;
  status: "OPEN" | "CLOSED" | "CANCELLED";
}

export type BookingStatus = "PENDING" | "CONFIRMED" | "CANCELLED" | "EXPIRED";
export type TravelerType = "ADULT" | "CHILD";

export interface BookingTraveler {
  id?: string;
  firstName: string;
  lastName: string;
  travelerType: TravelerType;
}

export interface BookingAddon {
  id?: string;
  code: string;
  label: string;
  priceCents: number;
}

export interface Booking {
  id: string;
  reference: string;
  status: BookingStatus;
  tourSlug: string;
  tourTitle: string;
  tourPlace: string;
  tourImageUrl: string;
  departureDate: string;
  travelersCount: number;
  priceCentsPerPerson: number;
  subtotalCents: number;
  addonsTotalCents: number;
  totalCents: number;
  currency: string;
  leadFirstName: string;
  leadLastName: string;
  leadEmail: string;
  leadPhone: string | null;
  leadCountry: string | null;
  expiresAt: string | null;
  createdAt: string;
  travelers: BookingTraveler[];
  addons: BookingAddon[];
}

export interface CreateBookingInput {
  tourSlug: string;
  departureDate: string;
  travelers: Array<{ firstName: string; lastName: string; travelerType: TravelerType }>;
  lead: {
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    country?: string;
  };
  addons: string[];
}

export type PaymentProvider = "STRIPE" | "PAYSTACK";

export interface PaymentInitResponse {
  provider: PaymentProvider;
  reference: string | null;   // Paystack transaction reference
  clientKey: string | null;   // Stripe client secret
  accessCode: string | null;  // Paystack access_code for popup resume
}

export interface AdminTourSummary {
  slug: string;
  title: string;
  place: string;
  destinationSlug: string;
  destinationName: string;
  durationDays: number;
  priceCents: number;
  currency: string;
  featured: boolean;
  displayOrder: number;
}

export interface AdminItineraryDay {
  dayNumber: number;
  title: string;
  description: string;
  accommodation: string | null;
  mealsIncluded: string | null;
}

export interface AdminTourDetail {
  slug: string;
  title: string;
  place: string;
  destinationSlug: string;
  durationDays: number;
  priceCents: number;
  currency: string;
  rating: number;
  reviewCount: number;
  tag: string;
  imageUrl: string;
  featured: boolean;
  displayOrder: number;
  styleSlugs: string[];
  itinerary: AdminItineraryDay[];
  inclusions: string[];
}

export interface TourWriteInput {
  title: string;
  slug?: string;
  place: string;
  destinationSlug: string;
  durationDays: number;
  priceCents: number;
  currency: string;
  rating: number;
  reviewCount: number;
  tag: string;
  imageUrl: string;
  featured: boolean;
  displayOrder: number;
  styleSlugs: string[];
  itinerary: AdminItineraryDay[];
  inclusions: string[];
}

export type IconName =
  | "arrow"
  | "chevron"
  | "down"
  | "pin"
  | "calendar"
  | "users"
  | "clock"
  | "heart"
  | "menu"
  | "close"
  | "search"
  | "check"
  | "shield"
  | "play"
  | "star"
  | "sliders"
  | "map";