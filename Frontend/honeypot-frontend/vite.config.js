import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    // The app talks to Spring Boot via VITE_API_URL (see .env.development).
    // This proxy is kept as a same-origin alternative: if you set
    // VITE_API_URL=/api the calls below will be forwarded to the backend.
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
