import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Button from "@/components/ui/Button";
import { useAuth } from "@/features/auth/AuthContext";
import { ApiError } from "@/lib/api";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: Location } | null)?.from?.pathname ?? "/";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong");
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="light-page">
      <Nav />
      <div className="auth-page">
        <div className="auth-card">
          <span className="eyebrow">Welcome back</span>
          <h1>Sign in</h1>
          <p className="text-muted">Continue planning your next journey.</p>

          <form onSubmit={submit}>
            <label>
              Email
              <input
                type="email"
                value={email}
                placeholder="janesmith@example.com"
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </label>
            <label>
              Password
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </label>
            {error && <p className="form-error">{error}</p>}
            <Button type="submit" disabled={busy}>
              {busy ? "Taking you there…" : "Sign in"}
            </Button>
          </form>

          <p className="auth-alt">
            New here? <Link to="/register">Create an account</Link>
          </p>
        </div>
      </div>
      <Footer />
    </main>
  );
}