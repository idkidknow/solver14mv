include "def.mzn";

predicate clue_is_not_mine(CellContext: ctx, array [int, int] of var bool: mines) =
  not(mines[ctx.i, ctx.j]);
