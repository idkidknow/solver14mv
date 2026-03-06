import { defineConfig, rspack } from '@rsbuild/core';

// Docs: https://rsbuild.rs/config/
export default defineConfig({
  html: {
    template: './static/index.html',
  },
  server: {
    headers: {
      'Cross-Origin-Embedder-Policy': 'require-corp',
      'Cross-Origin-Opener-Policy': 'same-origin',
    },
  },
  tools: {
    rspack: {
      plugins: [
        new rspack.CopyRspackPlugin({
          patterns: [
            {
              context: 'node_modules/z3-solver/build/',
              from: 'z3-built.*',
              to: 'static/',
            },
          ],
        }),
      ],
    },
  },
});
