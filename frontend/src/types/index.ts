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