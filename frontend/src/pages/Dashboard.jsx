import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { getMyLinks, getAllLinks, createLink, updateLink, deleteLink } from '../api';

/**
 * Dashboard — the main page for managing go links.
 *
 * Features:
 *   - View your go links
 *   - Browse all public go links
 *   - Create new go links
 *   - Edit/delete your own go links
 *
 * React concepts used:
 *   - useEffect: Runs code when the component mounts (like fetching data)
 *   - Conditional rendering: Show different UI based on state
 *   - List rendering: .map() to render a list of items
 */
export default function Dashboard() {
  const { user } = useAuth();
  const [myLinks, setMyLinks] = useState([]);
  const [allLinks, setAllLinks] = useState([]);
  const [showAll, setShowAll] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Form state
  const [showForm, setShowForm] = useState(false);
  const [editingKeyword, setEditingKeyword] = useState(null);
  const [formKeyword, setFormKeyword] = useState('');
  const [formUrl, setFormUrl] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  // Fetch links on component mount
  useEffect(() => {
    loadLinks();
  }, []);

  async function loadLinks() {
    try {
      setLoading(true);
      const [mine, all] = await Promise.all([getMyLinks(), getAllLinks()]);
      setMyLinks(mine);
      setAllLinks(all);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function openCreateForm() {
    setEditingKeyword(null);
    setFormKeyword('');
    setFormUrl('');
    setFormDescription('');
    setShowForm(true);
    setError('');
  }

  function openEditForm(link) {
    setEditingKeyword(link.keyword);
    setFormKeyword(link.keyword);
    setFormUrl(link.url);
    setFormDescription(link.description || '');
    setShowForm(true);
    setError('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setFormLoading(true);
    setError('');

    try {
      if (editingKeyword) {
        await updateLink(editingKeyword, formUrl, formDescription);
        setSuccess(`Updated go/${editingKeyword}`);
      } else {
        await createLink(formKeyword, formUrl, formDescription);
        setSuccess(`Created go/${formKeyword}`);
      }
      setShowForm(false);
      await loadLinks();
    } catch (err) {
      setError(err.message);
    } finally {
      setFormLoading(false);
    }
  }

  async function handleDelete(keyword) {
    if (!window.confirm(`Delete go/${keyword}? This cannot be undone.`)) return;

    try {
      await deleteLink(keyword);
      setSuccess(`Deleted go/${keyword}`);
      await loadLinks();
    } catch (err) {
      setError(err.message);
    }
  }

  // Auto-clear success message after 3 seconds
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  const displayLinks = showAll ? allLinks : myLinks;

  if (loading) {
    return (
      <div className="text-center mt-5">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>
          <i className="bi bi-grid me-2"></i>
          {showAll ? 'All Go Links' : 'My Go Links'}
        </h3>
        <div>
          <button
            className={`btn btn-outline-secondary me-2 ${showAll ? '' : 'active'}`}
            onClick={() => setShowAll(false)}
          >
            Mine ({myLinks.length})
          </button>
          <button
            className={`btn btn-outline-secondary me-2 ${showAll ? 'active' : ''}`}
            onClick={() => setShowAll(true)}
          >
            All ({allLinks.length})
          </button>
          <button className="btn btn-primary" onClick={openCreateForm}>
            <i className="bi bi-plus-lg me-1"></i> New Link
          </button>
        </div>
      </div>

      {/* Alerts */}
      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Create/Edit Form */}
      {showForm && (
        <div className="card mb-4 border-primary">
          <div className="card-body">
            <h5>{editingKeyword ? `Edit go/${editingKeyword}` : 'Create New Go Link'}</h5>
            <form onSubmit={handleSubmit}>
              <div className="row g-3">
                <div className="col-md-3">
                  <label className="form-label">Keyword</label>
                  <div className="input-group">
                    <span className="input-group-text">go/</span>
                    <input
                      type="text"
                      className="form-control"
                      value={formKeyword}
                      onChange={(e) => setFormKeyword(e.target.value.toLowerCase())}
                      disabled={!!editingKeyword}
                      required
                      placeholder="google"
                      pattern="[a-z0-9\-]+"
                      title="Lowercase letters, numbers, and hyphens only"
                    />
                  </div>
                </div>
                <div className="col-md-5">
                  <label className="form-label">URL</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formUrl}
                    onChange={(e) => setFormUrl(e.target.value)}
                    required
                    placeholder="https://www.google.com"
                  />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Description (optional)</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formDescription}
                    onChange={(e) => setFormDescription(e.target.value)}
                    placeholder="Google search engine"
                  />
                </div>
              </div>
              <div className="mt-3">
                <button type="submit" className="btn btn-primary me-2" disabled={formLoading}>
                  {formLoading && <span className="spinner-border spinner-border-sm me-1" />}
                  {editingKeyword ? 'Update' : 'Create'}
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => setShowForm(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Links Table */}
      {displayLinks.length === 0 ? (
        <div className="text-center text-muted mt-5">
          <i className="bi bi-link-45deg" style={{ fontSize: '3rem' }}></i>
          <p className="mt-2">
            {showAll ? 'No go links yet.' : 'You haven\'t created any go links yet.'}
          </p>
          <button className="btn btn-primary" onClick={openCreateForm}>
            Create your first go link
          </button>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead className="table-dark">
              <tr>
                <th>Shortcut</th>
                <th>Destination</th>
                <th>Description</th>
                {showAll && <th>Owner</th>}
                <th>Clicks</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayLinks.map((link) => (
                <tr key={link.keyword}>
                  <td>
                    <code className="fs-6">go/{link.keyword}</code>
                  </td>
                  <td>
                    <a href={link.url} target="_blank" rel="noopener noreferrer" className="text-truncate d-inline-block" style={{ maxWidth: '300px' }}>
                      {link.url}
                    </a>
                  </td>
                  <td className="text-muted">{link.description || '—'}</td>
                  {showAll && <td><span className="badge bg-secondary">{link.ownerUsername}</span></td>}
                  <td><span className="badge bg-info">{link.clickCount || 0}</span></td>
                  <td>
                    {(link.ownerUsername === user.username || user.role === 'ADMIN') && (
                      <>
                        <button
                          className="btn btn-sm btn-outline-primary me-1"
                          onClick={() => openEditForm(link)}
                          title="Edit"
                        >
                          <i className="bi bi-pencil"></i>
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDelete(link.keyword)}
                          title="Delete"
                        >
                          <i className="bi bi-trash"></i>
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
    </div>
  );
}
