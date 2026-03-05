<script lang="ts">
  import { initWith } from 'z3-solver'
  import * as solver from 'solver/main'
  let text = $state('');
  let initialized = $state(false);
  const test = async () => {
    text = await solver.test();
  };
  const init = async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const { Z3 } = await initWith((globalThis as any).initZ3);
    solver.initZ3(Z3);
    initialized = true;
  }
</script>

<button onclick={init}>init</button>
<button onclick={test}>test</button>
<p>{initialized ? 'initialized' : 'not initialized'}</p>
<p>{text}</p>
