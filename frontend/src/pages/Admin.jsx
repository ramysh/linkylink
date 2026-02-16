import { useState, useEffect } from 'react';
import { getUsers, updateUserRole, deleteUser, adminGetAllLinks, adminDeleteLink } from '../api';
import { useAuth } from '../AuthContext';

/**
 * Admin panel — manage users and all go links.
 * Only accessible to users with the ADMIN role.
 */
export default function Admin() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [links, setLinks] = useState([]);
  const [activeTab, setActiveTab] = useState('users');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const [usersData, linksData] = await Promise.all([getUsers(), adminGetAllLinks()]);
      setUsers(usersData);
      setLinks(linksData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRoleChange(username, newRole) {
    try {
      await updateUserRole(username, newRole);
      setSuccess(`Updated ${username}'s role to ${newRole}`);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDeleteUser(username) {
    if (username === user.username) {
      setError("You can't delete yourself!");
      return;
    }
    if (!window.confirm(`Delete user "${username}"? This cannot be undone.`)) return;

    try {
      await deleteUser(username);
      setSuccess(`Deleted user "${username}"`);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDeleteLink(keyword) {
    if (!window.confirm(`Delete go/${keyword}? This cannot be undone.`)) return;

    try {
      await adminDeleteLink(keyword);
      setSuccess(`Deleted go/${keyword}`);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  // Auto-clear messages
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  if (loading) {
    return (
      <div className="text-center mt-5">
        <div className="spinner-border" role="status" />
      </div>
    );
  }

  return (
    <div>
      <h3 className="mb-4">
        <i className="bi bi-shield-lock me-2"></i> Admin Panel
      </h3>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Tabs */}
      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            <i className="bi bi-people me-1"></i> Users ({users.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'links' ? 'active' : ''}`}
            onClick={() => setActiveTab('links')}
          >
            <i className="bi bi-link-45deg me-1"></i> All Links ({links.length})
          </button>
        </li>
      </ul>

      {/* Users Tab */}
      {activeTab === 'users' && (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead className="table-dark">
              <tr>
                <th>Username</th>
                <th>Role</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.username}>
                  <td>
                    <i className="bi bi-person-circle me-1"></i>
                    {u.username}
                    {u.username === user.username && (
                      <span className="badge bg-info ms-1">You</span>
                    )}
                  </td>
                  <td>
                    <span className={`badge ${u.role === 'ADMIN' ? 'bg-warning text-dark' : 'bg-secondary'}`}>
                      {u.role}
                    </span>
                  </td>
                  <td className="text-muted">
                    {new Date(u.createdAt).toLocaleDateString()}
                  </td>
                  <td>
                    {u.username !== user.username && (
                      <>
                        <button
                          className="btn btn-sm btn-outline-warning me-1"
                          onClick={() => handleRoleChange(u.username, u.role === 'ADMIN' ? 'USER' : 'ADMIN')}
                          title={u.role === 'ADMIN' ? 'Demote to User' : 'Promote to Admin'}
                        >
                          <i className={`bi ${u.role === 'ADMIN' ? 'bi-arrow-down' : 'bi-arrow-up'}`}></i>
                          {u.role === 'ADMIN' ? ' Demote' : ' Promote'}
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDeleteUser(u.username)}
                        >
                          <i className="bi bi-trash"></i> Delete
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Links Tab */}
      {activeTab === 'links' && (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead className="table-dark">
              <tr>
                <th>Shortcut</th>
                <th>Destination</th>
                <th>Owner</th>
                <th>Clicks</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {links.map((link) => (
                <tr key={link.keyword}>
                  <td><code>go/{link.keyword}</code></td>
                  <td>
                    <a href={link.url} target="_blank" rel="noopener noreferrer"
                       className="text-truncate d-inline-block" style={{ maxWidth: '300px' }}>
                      {link.url}
                    </a>
                  </td>
                  <td><span className="badge bg-secondary">{link.ownerUsername}</span></td>
                  <td><span className="badge bg-info">{link.clickCount || 0}</span></td>
                  <td>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDeleteLink(link.keyword)}
                    >
                      <i className="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
