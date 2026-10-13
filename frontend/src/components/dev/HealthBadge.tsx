import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { Health } from "@/types";

export default function HealthBadge() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["health"],
    queryFn: () => apiGet<Health>("/v1/health"),
    retry: 1,
    refetchOnWindowFocus: false,
  });

  if (!import.meta.env.DEV) return null;

  const style: React.CSSProperties = {
    position: "fixed",
    bottom: 12,
    right: 12,
    zIndex: 9999,
    padding: "6px 10px",
    fontSize: 11,
    fontFamily: "monospace",
    borderRadius: 6,
    background: "rgba(28,27,25,0.85)",
    color: "white",
    letterSpacing: "0.02em",
  };

  if (isLoading) return <div style={style}>backend: …</div>;
  if (error) return <div style={{ ...style, background: "#8f3f1e" }}>backend: unreachable</div>;
  if (data) return <div style={{ ...style, background: "#3f7a5a" }}>{data.service}: {data.status}</div>;
  return null;
}