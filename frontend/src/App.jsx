import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Admin from './pages/Admin';

/**
 * Main App component — defines the page routes.
 *
 * React Router v6 concepts:
 *   - <Routes>: Container for route definitions
 *   - <Route path="/login" element={<Login />}>: When URL matches /login, render Login component
 *   - <Navigate to="/login">: Redirect to another route
 *
 * PrivateRoute: A pattern that redirects to login if the user is not authenticated.
 */

function PrivateRoute({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" />;
}

function AdminRoute({ children }) {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (!isAdmin()) return <Navigate to="/dashboard" />;
  return children;
}

export default function App() {
  const { user } = useAuth();

  return (
    <div className="min-vh-100 bg-light">
      <Navbar />
      <div className="container py-4">
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <Login />} />
          <Route path="/register" element={user ? <Navigate to="/dashboard" /> : <Register />} />

          {/* Protected routes */}
          <Route path="/dashboard" element={
            <PrivateRoute><Dashboard /></PrivateRoute>
          } />

          {/* Admin route */}
          <Route path="/admin" element={
            <AdminRoute><Admin /></AdminRoute>
          } />

          {/* Default: redirect based on auth state */}
          <Route path="*" element={
            <Navigate to={user ? "/dashboard" : "/login"} />
          } />
        </Routes>
      </div>
    </div>
  );
}
