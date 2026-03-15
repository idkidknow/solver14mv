include "def.mzn";

function var int: neighboring_8_cross_count(CellContext: ctx, array [int, int] of var bool: mines) = let {
  int: i = ctx.i;
  int: j = ctx.j;
} in sum(kl in [(i - 2, j), (i - 1, j), (i + 1, j), (i + 2, j), (i, j - 2), (i, j - 1), (i, j + 1), (i, j + 2)] where
  (kl.1 > 0) /\ (kl.1 <= ctx.m) /\ (kl.2 > 0) /\ (kl.2 <= ctx.n)
)(mines[kl.1, kl.2]);

predicate clue_eq_neighboring_8_cross(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  value = neighboring_8_cross_count(ctx, mines);
