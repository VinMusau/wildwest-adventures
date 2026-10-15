import { useState } from "react";
import { Link, useNavigate } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Button from "@/components/ui/Button";
import { useAuth } from "@/features/auth/AuthContext";
import { ApiError } from "@/lib/api";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const update = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [key]: e.target.value });

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await register(form);
      navigate("/", { replace: true });
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
          <span className="eyebrow">Join us</span>
          <h1>Create your account</h1>
          <p className="text-muted">
            Save journeys, plan trips, and book securely.
          </p>

          <form onSubmit={submit}>
            <div className="auth-row">
              <label>
                First name
                <input value={form.firstName} onChange={update("firstName")} required autoComplete="given-name" />
              </label>
              <label>
                Last name
                <input value={form.lastName} onChange={update("lastName")} required autoComplete="family-name" />
              </label>
            </div>
            <label>
              Email
              <input type="email" value={form.email} onChange={update("email")} required autoComplete="email" />
            </label>
            <label>
              Password
              <input
                type="password"
                value={form.password}
                onChange={update("password")}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </label>
            {error && <p className="form-error">{error}</p>}
            <Button type="submit" disabled={busy}>
              {busy ? "Creating account…" : "Create account"}
            </Button>
          </form>

          <p className="auth-alt">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
      <Footer />
    </main>
  );
}