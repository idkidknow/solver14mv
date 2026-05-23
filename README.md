# solver14mv

A browser-based solver for [14 Minesweeper Variants](https://store.steampowered.com/app/1865060/14/).

## Usage

1. Visit [solver14mv.pages.dev](https://solver14mv.pages.dev/) (or build from source)
2. Enter the puzzle info: grid size, mine count and variant rules
3. Pick a clue type and click on the grid to place it
4. Click the **Solve** button. green cells are safe and red cells are mines

## Build from source

Prerequisites:

- Node.js >= 21
- pnpm 11

```sh
./mill build && pnpm build
```

### Development

Terminal 1, Scala.js watch build:
```sh
./mill -w build --dev
```

Terminal 2, Vite dev server:
```sh
pnpm dev
```
