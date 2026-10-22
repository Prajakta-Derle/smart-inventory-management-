import { createContext, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  registerTokenGetter,
  registerUnauthorizedHandler,
} from '../api/axiosInstance';

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext(null);

/**
 * Holds the JWT strictly in memory (React state) per the design's
 * in-memory-only token policy. The token is never written to localStorage,
 * sessionStorage, cookies, or IndexedDB, so a page refresh logs the user out.
 */
export function AuthProvider({ children }) {
  const [auth, setAuth] = useState({ token: null, role: null, username: null });

  // Keep a ref in sync so the axios interceptor reads the latest token at
  // call time without re-registering on every render.
  const tokenRef = useRef(null);
  tokenRef.current = auth.token;

  const login = useCallback((token, role, username) => {
    setAuth({ token, role, username });
  }, []);

  const logout = useCallback(() => {
    setAuth({ token: null, role: null, username: null });
  }, []);

  useEffect(() => {
    registerTokenGetter(() => tokenRef.current);
    registerUnauthorizedHandler(() => {
      // On a 401 from the backend, drop the in-memory token.
      setAuth({ token: null, role: null, username: null });
    });
  }, []);

  const value = useMemo(
    () => ({
      token: auth.token,
      role: auth.role,
      username: auth.username,
      isAuthenticated: Boolean(auth.token),
      isAdmin: auth.role === 'ADMIN',
      login,
      logout,
    }),
    [auth, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
