include "def.mzn";

function var int: neighboring_8_count(CellContext: ctx, array [int, int] of var bool: mines) = let {
  int: i = ctx.i;
  int: j = ctx.j;
} in sum(di, dj in {-1, 0, 1} where
  (i + di > 0) /\ (i + di <= ctx.m) /\
    (j + dj > 0) /\ (j + dj <= ctx.n) /\
    (i != 0 \/ j != 0)
)(mines[i + di, j + dj]);

function var int: neighboring_8_checkerboard_count(
  CellContext: ctx,
  array [int, int] of var bool: mines,
  int: weight_uncolored,
  int: weight_colored,
) = let {
  int: i = ctx.i;
  int: j = ctx.j;
} in sum(di, dj in {-1, 0, 1} where
  (i + di > 0) /\ (i + di <= ctx.m) /\
    (j + dj > 0) /\ (j + dj <= ctx.n) /\
    (i != 0 \/ j != 0)
)(
  let {
    int: k = i + di;
    int: l = j + dj;
    int: weight = if (k + l) mod 2 = 0 then weight_uncolored else weight_colored endif;
  } in mines[i + di, j + dj] * weight
);

predicate clue_eq_neighboring_8(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  value = neighboring_8_count(ctx, mines);

predicate clue_eq_neighboring_8_checkerboard_weighted_1_2(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  value = neighboring_8_checkerboard_count(ctx, mines, 1, 2);

predicate clue_eq_neighboring_8_checkerboard_diff(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  value = abs(neighboring_8_checkerboard_count(ctx, mines, 1, -1));

predicate clue_diff_neighboring_8_eq_1(CellContext: ctx, int: value, array [int, int] of var bool: mines) =
  abs(value - neighboring_8_count(ctx, mines)) = 1;
