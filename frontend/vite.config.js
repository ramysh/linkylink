import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  // Base path: React app is served from /app/ in production
  base: '/app/',

  // Build output goes into Spring Boot's static resources folder
  build: {
    outDir: '../src/main/resources/static/app',
    emptyOutDir: true,
  },

  // Development server config
  server: {
    port: 5173,

    // Proxy API requests to the Spring Boot backend during development.
    // When React code calls fetch('/api/links'), Vite forwards it to localhost:8080.
    // This avoids CORS issues during development.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
