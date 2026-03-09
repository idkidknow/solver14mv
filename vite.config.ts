import path from "node:path";
import { defineConfig } from "vite";

export default defineConfig({
  resolve: {
    alias: {
      "@scala": path.resolve(__dirname, "./out/solver14mv/dev.dest"),
    },
  },
});
