include "def.mzn";
include "clue_is_not_mine.mzn";
include "total_mine_count_eq.mzn";
include "no_triplets.mzn";
include "8_connected.mzn";
include "quad_ge_1.mzn";
include "4_connected.mzn";
include "8_connected.mzn";
include "neighboring_4.mzn";
include "neighboring_8.mzn";
include "row_column_wise.mzn";
include "consecutive.mzn";

enum ClueType = { QuestionMark, Vanilla, Multiple, Liar, Wall, Negation };

type Clue = record(
  int: i,
  int: j,
  ClueType: ty,
  % QuestionMark: meaningless, always 0
  % Vanilla/Multiple/Liar/Negation: the value
  % Wall: concatenate digits into a decimal number
  int: data,
);

enum Rule = { Quad, Connected, Triplet, Outside, Dual, Snake, Balance };

int: m;
int: n;
array [int] of Clue: clues;
set of Rule: rules;
int: mine_count;
record(int: i, int: j, bool: is_mine): assert_mine;

array [1..m, 1..n] of var bool: mines;

constraint mines[assert_mine.i, assert_mine.j] = assert_mine.is_mine;

constraint forall(clue in clues)(
  let {
    CellContext: ctx = (m: m, n: n, i: clue.i, j: clue.j);
    ClueType: ty = clue.ty;
  } in
    if ty = QuestionMark then clue_is_not_mine(ctx, mines)
    elseif ty = Vanilla then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_neighboring_8(ctx, clue.data, mines)
    elseif ty = Multiple then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_neighboring_8_checkerboard_weighted_1_2(ctx, clue.data, mines)
    elseif ty = Liar then
      clue_is_not_mine(ctx, mines) /\
        clue_diff_neighboring_8_eq_1(ctx, clue.data, mines)
    elseif ty = Wall then let {
      int: data = clue.data;
      array [1..4] of 0..8: data_arr = [data mod 10, data div 10 mod 10, data div 100 mod 10, data div 1000 mod 10];
    } in clue_is_not_mine(ctx, mines) /\
        consecutive_lengths(ctx, data_arr, mines)
    elseif ty = Negation then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_neighboring_8_checkerboard_diff(ctx, clue.data, mines)
    else true endif
);

constraint forall(rule in rules)(
  if rule = Quad then quad_ge_1(m, n, mines)
  elseif rule = Connected then mines_8_connected(m, n, mines)
  elseif rule = Triplet then no_triplets(m, n, mines)
  elseif rule = Outside then
    non_mines_4_connected(m, n, mines) /\ mines_4_connected_to_outside(m, n, mines)
  elseif rule = Dual then mines_neighboring_4_eq_1(m, n, mines)
  elseif rule = Snake then
    mines_4_connected(m, n, mines) /\
      mines_neighboring_4_le_2(m, n, mines) /\
      exists_mines_neighboring_4_eq_1(m, n, mines)
  elseif rule = Balance then same_row_column_sum(m, n, mines)
  else true endif
);

constraint mine_count > 0 -> total_mine_count_eq(m, n, mines, mine_count);

solve satisfy;
