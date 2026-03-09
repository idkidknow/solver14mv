include "def.mzn";

predicate total_mine_count_eq(BoardData: board, array [int, int] of var bool: mines, int: cnt) = let {
  int: m = board.m;
  int: n = board.n;
  array [int, int] of -2..8: clues = board.clues;
} in sum(i in 1..m, j in 1..n)(mines[i, j]) = cnt;
