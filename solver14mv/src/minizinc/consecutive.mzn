include "def.mzn";
include "alldifferent.mzn";

predicate consecutive_lengths(CellContext: ctx, array [1..4] of 0..8: lengths, array [int, int] of var bool: mines) = let {
  int: m = ctx.m;
  int: n = ctx.n;
  int: i = ctx.i;
  int: j = ctx.j;
  array [0..7] of var bool: n8 = array1d(0..7,
    [ if kl.1 > 0 /\ kl.1 <= m /\ kl.2 > 0 /\ kl.2 <= n then mines[kl.1, kl.2] else false endif |
      kl in [(i - 1, j - 1), (i - 1, j), (i - 1, j + 1), (i, j + 1), (i + 1, j + 1), (i + 1, j), (i + 1, j - 1), (i, j - 1)]
    ]
  );
  array [1..4] of var 0..7: pre0;
  var bool: match = forall(i in 1..4)(
    (lengths[i] != 0 /\ lengths[i] != 8) -> (
      not(n8[pre0[i]]) /\ forall(d in 1..lengths[i])(n8[(pre0[i] + d) mod 8]) /\ not(n8[(pre0[i] + lengths[i] + 1) mod 8])
    )
  );
} in alldifferent(pre0) /\ match /\ sum(n8) = sum(lengths);
