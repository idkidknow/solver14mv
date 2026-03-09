include "def.mzn";
include "clues_are_not_mines.mzn";
include "clue_eq_neighboring_8.mzn";
include "total_mine_count_eq.mzn";
include "no_triplets.mzn";
include "8_connected.mzn";
include "quad_ge_1.mzn";
include "4_connected.mzn";
include "mines_neighboring_4_eq_1.mzn";
include "mines_neighboring_4_le_2.mzn";

int: m;
int: n;
array [1..m, 1..n] of -2..8: clues;

record(
  bool: clues_are_not_mines,
  bool: clue_eq_neighboring_8,
  int: total_mine_count_eq,
  bool: no_triplets,
  bool: mines_8_connected,
  bool: quad_ge_1,
  bool: mines_4_connected,
  bool: mines_4_connected_to_outside,
  bool: non_mines_4_connected,
  bool: mines_neighboring_4_eq_1,
  bool: mines_neighboring_4_le_2,
): constraints;

record(int: i, int: j, bool: is_mine): assert_mine;

array [1..board.m, 1..board.n] of var bool: mines;

BoardData: board = (m: m, n: n, clues: clues);

constraint constraints.clues_are_not_mines -> clues_are_not_mines(board, mines);
constraint constraints.clue_eq_neighboring_8 -> clue_eq_neighboring_8(board, mines);
constraint if constraints.total_mine_count_eq > 0 then total_mine_count_eq(board, mines, constraints.total_mine_count_eq) else true endif;
constraint constraints.no_triplets -> no_triplets(board, mines);
constraint constraints.mines_8_connected -> mines_8_connected(board, mines);
constraint constraints.quad_ge_1 -> quad_ge_1(board, mines);
constraint constraints.mines_4_connected -> mines_4_connected(board, mines);
constraint constraints.mines_4_connected_to_outside -> mines_4_connected_to_outside(board, mines);
constraint constraints.non_mines_4_connected -> non_mines_4_connected(board, mines);
constraint constraints.mines_neighboring_4_eq_1 -> mines_neighboring_4_eq_1(board, mines);
constraint constraints.mines_neighboring_4_le_2 -> mines_neighboring_4_le_2(board, mines);

constraint mines[assert_mine.i, assert_mine.j] = assert_mine.is_mine;

solve satisfy;
