import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { authApi, type LoginInput, type RegisterInput } from "./api";
import { AUTH_EXPIRED_EVENT, tokenStorage } from "./storage";
import { useToast } from "@/features/toast/useToast";
import type { User } from "@/types";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (input: LoginInput) => Promise<User>;
  register: (input: RegisterInput) => Promise<User>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const toast = useToast();

  // On mount: if a token exists, try to fetch the current user
  useEffect(() => {
    const token = tokenStorage.get();
    if (!token) {
      setLoading(false);
      return;
    }
    
    authApi
      .me()
      .then(setUser)
      .catch(() => {
        // Silently clear the token and user on initial load failure.
        tokenStorage.clear();
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  // Listen for 401s from the API layer
  useEffect(() => {
    const handler = (e: Event) => {
      setUser(null);
      
      // Only show the toast if the API didn't mark the event as "silent"
      const customEvent = e as CustomEvent;
      if (!customEvent.detail?.silent) {
        toast.info("Your session has expired", { 
          description: "Please log in again." 
        });
      }
    };
    
    window.addEventListener(AUTH_EXPIRED_EVENT, handler);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, handler);
  }, [toast]); 
  
  const login = useCallback(async (input: LoginInput): Promise<User> => {
    const res = await authApi.login(input);
    tokenStorage.set(res.token);
    setUser(res.user);
    return res.user;
  }, []);

  const register = useCallback(async (input: RegisterInput): Promise<User> => {
    const res = await authApi.register(input);
    tokenStorage.set(res.token);
    setUser(res.user);
    return res.user;
  }, []);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}