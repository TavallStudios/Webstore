import { resolve } from "node:path";
import { defineConfig } from "vite";

const projectRoot = resolve(process.cwd());
const staticRoot = resolve(projectRoot, "src/main/resources/static");
const backendOrigin = process.env.VITE_BACKEND_ORIGIN || "http://127.0.0.1:8080";

export default defineConfig({
  root: staticRoot,
  appType: "custom",
  publicDir: false,
  server: {
    host: "127.0.0.1",
    port: 5173,
    strictPort: true,
    cors: true,
    proxy: {
      "^/(admin|cart|tracking|products|media|theme-assets|checkout|actuator)(/.*)?$": {
        target: backendOrigin,
        changeOrigin: true
      }
    }
  },
  preview: {
    host: "127.0.0.1",
    port: 4173,
    strictPort: true
  },
  build: {
    outDir: resolve(projectRoot, "target/vite-assets"),
    emptyOutDir: true,
    sourcemap: true,
    rollupOptions: {
      input: {
        admin: resolve(staticRoot, "assets/admin/admin.js"),
        storefront: resolve(staticRoot, "assets/storefront/storefront.js")
      }
    }
  }
});
