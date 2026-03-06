declare module 'solver14mv-scala' {
  import type { Z3LowLevel } from 'z3-solver';
  export function test(): void;

  export function renderApp(
    root: Element,
    getZ3: () => Promise<Z3LowLevel['Z3']>,
  ): void;
}
