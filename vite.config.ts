import path from "node:path";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [tailwindcss()],
  resolve: {
    alias: {
      "@": import.meta.dirname,
      "@scala": path.resolve(import.meta.dirname, "./out/build.dest"),
      "@src": path.resolve(import.meta.dirname, "./solver14mv/src"),
    },
  },
  server: {
    watch: {
      ignored: ["**/*.scala"],
    },
  },
});
