import path from "node:path";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [tailwindcss()],
  resolve: {
    alias: {
      "@": __dirname,
      "@scala": path.resolve(__dirname, "./out/build.dest"),
      "@src": path.resolve(__dirname, "./solver14mv/src"),
    },
  },
  server: {
    watch: {
      ignored: ["**/*.scala"],
    },
  },
});
