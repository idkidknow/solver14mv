include "def.mzn";

function var int: knight_count(CellContext: ctx, array [int, int] of var bool: mines) = let {
  int: i = ctx.i;
  int: j = ctx.j;
} in sum(kl in [(i - 1, j - 2), (i - 2, j - 1), (i - 2, j + 1), (i - 1, j + 2), (i + 1, j + 2), (i + 2, j + 1), (i + 2, j - 1), (i + 1, j - 2)] where
  (kl.1 > 0) /\ (kl.1 <= ctx.m) /\ (kl.2 > 0) /\ (kl.2 <= ctx.n)
)(mines[kl.1, kl.2]);

predicate clue_eq_knight_count(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  value = knight_count(ctx, mines);
