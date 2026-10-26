import { defineConfig, transformWithOxc } from 'vite';
import fs from 'node:fs';
import path from 'node:path';
import { devServer } from './build/dev-server.mjs';

// The shell fetches /<name>/<file> and reads the routes off window[<name>], so both come from package.json.
const pkg = JSON.parse(fs.readFileSync(path.resolve(import.meta.dirname, 'package.json'), 'utf8'));
const self = { name: pkg.name, file: path.basename(pkg.main) };

const frontend = path.resolve(import.meta.dirname, 'frontend') + path.sep;

// The sources are .js with JSX in them. Classic runtime, so React comes from perun-core.
const jsxInJs = () => ({
  name: `${self.name}:jsx-in-js`,
  enforce: 'pre',
  transform(code, id) {
    if (!id.startsWith(frontend) || !id.endsWith('.js')) return null;
    return transformWithOxc(code, id, { lang: 'jsx', jsx: { runtime: 'classic' } });
  },
});

// Relative to the repository, so the map's sources stay under /<name>/ and do not meet another module's
// frontend/index.js in DevTools.
const sourcePath = (source, map) =>
  path.relative(import.meta.dirname, path.resolve(path.dirname(map), source)).split(path.sep).join('/');

export default defineConfig(({ mode }) => ({
  publicDir: false,
  plugins: [jsxInJs(), devServer(self)],
  build: {
    outDir: 'backend/www',
    // backend/www also holds index.html and config.js.
    emptyOutDir: false,
    target: 'es2020',
    sourcemap: true,
    minify: mode === 'production',
    lib: {
      // The development entry also registers the plugin with perun-core's plugin manager.
      entry: mode === 'production' ? 'frontend/index.js' : 'frontend/client.js',
      name: self.name,
      formats: ['umd'],
      fileName: () => self.file,
    },
    rollupOptions: {
      external: ['perun-core'],
      output: {
        globals: { 'perun-core': 'perun-core' },
        sourcemapPathTransform: sourcePath,
      },
    },
  },
}));
