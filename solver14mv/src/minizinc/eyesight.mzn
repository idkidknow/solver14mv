include "def.mzn";

predicate clue_eq_eyesight(CellContext: ctx, int: value, array [int, int] of var bool: mines) = let {
  int: m = ctx.m;
  int: n = ctx.n;
  int: i = ctx.i;
  int: j = ctx.j;
  var 1..i: i1;
  var i..m: i2;
  var 1..j: j1;
  var j..n: j2;
  var bool: c1 = (i1 > 1 -> mines[i1 - 1, j]) /\ forall(ii in i1..i)(not(mines[ii, j]));
  var bool: c2 = (i2 < m -> mines[i2 + 1, j]) /\ forall(ii in i..i2)(not(mines[ii, j]));
  var bool: c3 = (j1 > 1 -> mines[i, j1 - 1]) /\ forall(jj in j1..j)(not(mines[i, jj]));
  var bool: c4 = (j2 < n -> mines[i, j2 + 1]) /\ forall(jj in j..j2)(not(mines[i, jj]));
} in c1 /\ c2 /\ c3 /\ c4 /\ i2 - i1 + j2 - j1 + 1 = value;

