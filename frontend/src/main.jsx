import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { AuthProvider } from './AuthContext';
import './App.css';

/**
 * Entry point for the React app.
 *
 * BrowserRouter: Enables client-side routing (URL changes without page reload).
 *   - basename="/app" means all routes are prefixed with /app
 *   - So <Route path="/dashboard"> becomes /app/dashboard in the URL
 *
 * AuthProvider: Makes authentication state (user, token) available
 *   to all child components via React Context.
 *
 * StrictMode: Development-only checks for common React mistakes.
 */
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter basename="/app">
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);
