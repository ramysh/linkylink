import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

/**
 * Navigation bar — shown on every page.
 *
 * Shows different links based on auth state:
 *   - Logged out: Login, Register
 *   - Logged in:  Dashboard, Admin (if admin), Logout
 */
export default function Navbar() {
  const { user, logoutUser, isAdmin } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logoutUser();
    navigate('/login');
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        {/* Brand */}
        <Link className="navbar-brand fw-bold" to="/">
          <i className="bi bi-link-45deg"></i> LinkyLink
        </Link>

        {/* Mobile toggle button */}
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        {/* Nav links */}
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto">
            {user && (
              <li className="nav-item">
                <Link className="nav-link" to="/dashboard">
                  <i className="bi bi-grid"></i> Dashboard
                </Link>
              </li>
            )}
            {user && isAdmin() && (
              <li className="nav-item">
                <Link className="nav-link" to="/admin">
                  <i className="bi bi-shield-lock"></i> Admin
                </Link>
              </li>
            )}
          </ul>

          {/* Right side */}
          <ul className="navbar-nav">
            {user ? (
              <>
                <li className="nav-item">
                  <span className="nav-link text-light">
                    <i className="bi bi-person-circle"></i>{' '}
                    {user.username}
                    {isAdmin() && (
                      <span className="badge bg-warning text-dark ms-1">Admin</span>
                    )}
                  </span>
                </li>
                <li className="nav-item">
                  <button className="nav-link btn btn-link" onClick={handleLogout}>
                    <i className="bi bi-box-arrow-right"></i> Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="nav-item">
                  <Link className="nav-link" to="/login">Login</Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/register">Register</Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
}
