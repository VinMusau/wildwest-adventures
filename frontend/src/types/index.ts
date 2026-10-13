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
  title: string;
  description: string;
  image: string;
}

export interface Destination {
  name: string;
  journeyCount: string;
  image: string;
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