export interface Tour {
  slug: string;
  title: string;
  place: string;
  days: string;
  price: string;
  rating: string;
  reviews: number;
  image: string;
  tag: string;
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