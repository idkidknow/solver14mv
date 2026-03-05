<script lang="ts">
  import { initWith } from 'z3-solver';
  import * as solver from 'solver/main';
	import Recognize from '$lib/Recognize.svelte';
	import type { Clue } from '$lib/clue';

  let initialized = $state(false);
  let clues: Clue[] = $state([]);
  let m = $state(0);
  let n = $state(0);
  let mineCount = $state(0);
  let resultText = $state('');

  const solve = async () => {
    if (!initialized) {
      await init();
    }
    resultText = await solver.test(m, n, mineCount, clues);
  };

  const init = async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const { Z3 } = await initWith((globalThis as any).initZ3);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (solver as any).initZ3(Z3);
    initialized = true;
  }
</script>

<h1>14 Minesweeper Variants [T]</h1>

<button onclick={init}>init</button>
<p>{initialized ? 'initialized' : 'not initialized'}</p>

<Recognize onComplete={(ret) => clues = ret} />

<div>
  <p>Clues:</p>
  <ul>
    {#each clues as clue (clue)}
      <li>{clue.type === "number" ? `${clue.i}, ${clue.j}: ${clue.value}` : `${clue.i}, ${clue.j}: ?`}</li>
    {/each}
  </ul>

  total mine count: <input type="number" bind:value={mineCount} /><br />
  rows: <input type="number" bind:value={m} /><br />
  columns: <input type="number" bind:value={n} /><br />
  <button onclick={solve}>Solve</button>
  <p>{resultText}</p>
</div>
