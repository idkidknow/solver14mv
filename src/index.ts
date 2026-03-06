import { renderApp } from 'solver14mv-scala';

// import { initWith } from 'z3-solver';
import { init } from 'z3-solver';

const root = document.querySelector('#app');

if (root) {
  renderApp(root, async () => {
    const { Z3 } = await init();
    return Z3;
  });
}
