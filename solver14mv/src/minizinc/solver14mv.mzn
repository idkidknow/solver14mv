include "def.mzn";
include "clue_is_not_mine.mzn";
include "total_mine_count_eq.mzn";
include "tuplet.mzn";
include "8_connected.mzn";
include "quad_ge_1.mzn";
include "4_connected.mzn";
include "8_connected.mzn";
include "neighboring_4.mzn";
include "neighboring_8.mzn";
include "row_column_wise.mzn";
include "consecutive.mzn";
include "neighboring_8_cross.mzn";
include "eyesight.mzn";
include "knight.mzn";
include "must_in_triplets.mzn";

enum ClueType = {
  QuestionMark, Flagged,
  Vanilla, Multiple, Liar, Wall, Negation, Cross, Partition, Eyesight,
  MiniCross, Knight, LongestWall, EyesightPrime,
};

type Clue = record(
  int: i,
  int: j,
  ClueType: ty,
  % QuestionMark: meaningless, always 0
  % Flagged: meaningless, always 0
  % Wall: concatenate digits into a decimal number
  % EyesightPrime: horizontal - vertical
  % Other: the value
  int: data,
);

enum Rule = {
  Quad, Connected, Triplet, Outside, Dual, Snake, Balance,
  TripletPrime, Battleship, AntiKnight, Horizontal, Unary,
};

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
    elseif ty = Flagged then not(clue_is_not_mine(ctx, mines))
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
        clue_is_consecutive_lengths(ctx, data_arr, mines)
    elseif ty = Negation then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_neighboring_8_checkerboard_diff(ctx, clue.data, mines)
    elseif ty = Cross then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_neighboring_8_cross(ctx, clue.data, mines)
    elseif ty = Partition then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_consecutive_group_count(ctx, clue.data, mines)
    elseif ty = Eyesight then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_eyesight(ctx, clue.data, mines)
    elseif ty = MiniCross then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_neighboring_4(ctx, clue.data, mines)
    elseif ty = Knight then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_knight_count(ctx, clue.data, mines)
    elseif ty = LongestWall then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_longest_consecutive_length(ctx, clue.data, mines)
    elseif ty = EyesightPrime then
      clue_is_not_mine(ctx, mines) /\
        clue_eq_eyesight_diff(ctx, clue.data, mines)
    else true endif
);

constraint forall(rule in rules)(
  if rule = Quad then quad_ge_1(m, n, mines)
  elseif rule = Connected then mines_8_connected(m, n, mines)
  elseif rule = Triplet then no_triplets_of_mines(m, n, mines)
  elseif rule = Outside then
    non_mines_4_connected(m, n, mines) /\ mines_4_connected_to_outside(m, n, mines)
  elseif rule = Dual then mines_neighboring_4_eq_1(m, n, mines)
  elseif rule = Snake then
    mines_4_connected(m, n, mines) /\
      mines_neighboring_4_le_2(m, n, mines) /\
      exists_mines_neighboring_4_eq_1(m, n, mines)
  elseif rule = Balance then same_row_column_sum(m, n, mines)
  elseif rule = TripletPrime then must_in_triplets(m, n, mines)
  elseif rule = Battleship then
    no_skew_duplets_of_mines(m, n, mines) /\
      no_horizontal_quintuplets_of_mines(m, n, mines) /\
      no_vertical_quintuplets_of_mines(m, n, mines)
  elseif rule = AntiKnight then no_knights_move_duplets_of_mines(m, n, mines)
  elseif rule = Horizontal then no_horizontal_duplets_of_mines(m, n, mines)
  elseif rule = Unary then mines_neighboring_4_eq_0(m, n, mines)
  else true endif
);

constraint mine_count > 0 -> total_mine_count_eq(m, n, mines, mine_count);

solve satisfy;
