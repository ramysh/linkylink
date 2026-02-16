import { createContext, useContext, useState } from 'react';

/**
 * React Context for authentication state.
 *
 * Context lets you share data (like "who is logged in?") across ALL components
 * without passing props through every level. Think of it as "global state."
 *
 * How it works:
 *   1. AuthProvider wraps the entire app (in main.jsx)
 *   2. Any child component can call useAuth() to access the user and token
 *   3. When user logs in/out, ALL components that use useAuth() re-render
 *
 * Data stored:
 *   - user: { username, role } or null
 *   - token: JWT string or null
 */

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // Initialize from localStorage (so auth persists across page refreshes)
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('linkylink_user');
    return stored ? JSON.parse(stored) : null;
  });

  const [token, setToken] = useState(() => {
    return localStorage.getItem('linkylink_token');
  });

  /**
   * Call after successful login/registration.
   */
  function loginUser(token, username, role) {
    const userData = { username, role };
    setUser(userData);
    setToken(token);
    localStorage.setItem('linkylink_token', token);
    localStorage.setItem('linkylink_user', JSON.stringify(userData));
  }

  /**
   * Call to log out.
   */
  function logoutUser() {
    setUser(null);
    setToken(null);
    localStorage.removeItem('linkylink_token');
    localStorage.removeItem('linkylink_user');
  }

  /**
   * Check if the current user is an admin.
   */
  function isAdmin() {
    return user?.role === 'ADMIN';
  }

  return (
    <AuthContext.Provider value={{ user, token, loginUser, logoutUser, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Custom hook to access auth state from any component.
 * Usage: const { user, loginUser, logoutUser, isAdmin } = useAuth();
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
