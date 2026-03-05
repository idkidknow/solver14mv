import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

import { viteStaticCopy } from 'vite-plugin-static-copy';

export default defineConfig({
	plugins: [
		sveltekit(),
		viteStaticCopy({
			targets: [{ src: 'node_modules/z3-solver/build/z3-built.*', dest: '' }]
		}),
		{
			name: 'allow-shared-array-buffer',
			configureServer: (server) => {
				server.middlewares.use((_req, res, next) => {
					res.setHeader('Cross-Origin-Embedder-Policy', 'require-corp');
					res.setHeader('Cross-Origin-Opener-Policy', 'same-origin');
					next();
				});
			}
		}
	],
	server: {
		fs: {
			allow: ['./solver']
		}
	},
	resolve: {
		alias: {
			'solver/*': './solver/*'
		}
	}
});
