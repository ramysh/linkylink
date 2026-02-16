/**
 * API helper — centralizes all HTTP calls to the Spring Boot backend.
 *
 * Key concepts:
 *   - fetch(): The browser's built-in HTTP client (no library needed)
 *   - JWT tokens are sent in the "Authorization: Bearer <token>" header
 *   - The token is stored in localStorage (persists across page refreshes)
 */

const API_BASE = '/api';

/**
 * Get the stored JWT token.
 */
function getToken() {
  return localStorage.getItem('linkylink_token');
}

/**
 * Build headers for API requests.
 * Includes the JWT token if the user is logged in.
 */
function authHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

/**
 * Generic fetch wrapper with error handling.
 */
async function request(url, options = {}) {
  const response = await fetch(API_BASE + url, {
    headers: authHeaders(),
    ...options,
  });

  // If unauthorized, clear token and redirect to login
  if (response.status === 401) {
    localStorage.removeItem('linkylink_token');
    localStorage.removeItem('linkylink_user');
    window.location.href = '/app/login';
    return null;
  }

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.error || 'Something went wrong');
  }

  return data;
}

// ==================== Auth API ====================

export async function login(username, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

export async function register(username, password) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

// ==================== Links API ====================

export async function getMyLinks() {
  return request('/links');
}

export async function getAllLinks() {
  return request('/links/all');
}

export async function createLink(keyword, url, description) {
  return request('/links', {
    method: 'POST',
    body: JSON.stringify({ keyword, url, description }),
  });
}

export async function updateLink(keyword, url, description) {
  return request(`/links/${keyword}`, {
    method: 'PUT',
    body: JSON.stringify({ keyword, url, description }),
  });
}

export async function deleteLink(keyword) {
  return request(`/links/${keyword}`, {
    method: 'DELETE',
  });
}

// ==================== Admin API ====================

export async function getUsers() {
  return request('/admin/users');
}

export async function updateUserRole(username, role) {
  return request(`/admin/users/${username}/role`, {
    method: 'PUT',
    body: JSON.stringify({ role }),
  });
}

export async function deleteUser(username) {
  return request(`/admin/users/${username}`, {
    method: 'DELETE',
  });
}

export async function adminGetAllLinks() {
  return request('/admin/links');
}

export async function adminDeleteLink(keyword) {
  return request(`/admin/links/${keyword}`, {
    method: 'DELETE',
  });
}
