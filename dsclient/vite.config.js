import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: "https://localhost:8080",
        changeOrigin: true,
        secure: false, // only for local deployment, disables check for SSL certificate
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
