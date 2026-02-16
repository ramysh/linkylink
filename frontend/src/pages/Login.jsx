import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { login } from '../api';

/**
 * Login page.
 *
 * React concepts used:
 *   - useState: Manages component-local state (form inputs, error messages)
 *   - useNavigate: Programmatic navigation (redirect after login)
 *   - Controlled inputs: Input values are driven by React state
 */
export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { loginUser } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault(); // Prevent default form submission (page reload)
    setError('');
    setLoading(true);

    try {
      const data = await login(username, password);
      loginUser(data.token, data.username, data.role);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-md-5">
        <div className="card shadow-sm mt-5">
          <div className="card-body p-4">
            <h2 className="text-center mb-4">
              <i className="bi bi-link-45deg"></i> LinkyLink
            </h2>
            <h5 className="text-center text-muted mb-4">Sign in to your account</h5>

            {error && (
              <div className="alert alert-danger" role="alert">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="username" className="form-label">Username</label>
                <input
                  type="text"
                  className="form-control"
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  autoFocus
                />
              </div>

              <div className="mb-3">
                <label htmlFor="password" className="form-label">Password</label>
                <input
                  type="password"
                  className="form-control"
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary w-100"
                disabled={loading}
              >
                {loading ? (
                  <span className="spinner-border spinner-border-sm me-2" role="status" />
                ) : (
                  <i className="bi bi-box-arrow-in-right me-2"></i>
                )}
                Sign In
              </button>
            </form>

            <p className="text-center mt-3 mb-0">
              Don't have an account?{' '}
              <Link to="/register">Register here</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
