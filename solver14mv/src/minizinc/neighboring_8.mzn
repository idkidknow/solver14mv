include "def.mzn";

function var int: neighboring_8_count(CellContext: ctx, array [int, int] of var bool: mines) = let {
  int: i = ctx.i;
  int: j = ctx.j;
} in sum(di, dj in {-1, 0, 1} where
  (i + di > 0) /\ (i + di <= ctx.m) /\
    (j + dj > 0) /\ (j + dj <= ctx.n) /\
    (i != 0 \/ j != 0)
)(mines[i + di, j + dj]);

predicate clue_eq_neighboring_8(CellContext: ctx, int: value, array [int, int] of var bool: mines) = let {
  int: m = ctx.m;
  int: n = ctx.n;
  int: i = ctx.i;
  int: j = ctx.j;
} in value = neighboring_8_count(ctx, mines);
