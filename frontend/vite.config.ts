import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_');
  if (command === 'build' && mode !== 'development') {
    const value = process.env.VITE_API_BASE_URL || env.VITE_API_BASE_URL;
    if (!value) throw new Error('Production requires VITE_API_BASE_URL=https://<render-service>.onrender.com/api');
    const url = new URL(value);
    if (url.protocol !== 'https:' || /^(localhost$|127\.|0\.0\.0\.0$|\[?::1\]?$)/i.test(url.hostname) || url.username || url.password || url.search || url.hash || url.pathname !== '/api') {
      throw new Error('VITE_API_BASE_URL must be an HTTPS backend URL ending in /api, without credentials or query parameters.');
    }
  }
  return {
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  };
});
