include "def.mzn";

function var int: neighboring_4_count(CellContext: ctx, array [int, int] of var bool: mines) = let {
  int: i = ctx.i;
  int: j = ctx.j;
} in sum(kl in [(i - 1, j), (i, j - 1), (i, j + 1), (i + 1, j)] where
  (kl.1 > 0) /\ (kl.1 <= ctx.m) /\ (kl.2 > 0) /\ (kl.2 <= ctx.n)
)(mines[kl.1, kl.2]);

predicate mines_neighboring_4_le_2(int: m, int: n, array [int, int] of var bool: mines) =
  forall(i in 1..m, j in 1..n)(
    mines[i, j] -> neighboring_4_count((m: m, n: n, i: i, j: j), mines) <= 2
  );

predicate mines_neighboring_4_eq_1(int: m, int: n, array [int, int] of var bool: mines) =
  forall(i in 1..m, j in 1..n)(
    mines[i, j] -> neighboring_4_count((m: m, n: n, i: i, j: j), mines) = 1
  );

predicate exists_mines_neighboring_4_eq_1(int: m, int: n, array [int, int] of var bool: mines) =
  exists(i in 1..m, j in 1..n)(
    mines[i, j] -> neighboring_4_count((m: m, n: n, i: i, j: j), mines) = 1
  );

predicate clue_eq_neighboring_4(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  value = neighboring_4_count(ctx, mines);
