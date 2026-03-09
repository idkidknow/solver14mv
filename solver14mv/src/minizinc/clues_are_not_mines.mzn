include "def.mzn";

predicate clues_are_not_mines(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  array [int, int] of -2..8: clues = board.clues;
} in forall(i in 1..m, j in 1..n)(
  if clues[i, j] >= 0 \/ clues[i, j] = -2 then
    not(mines[i, j])
  else true endif
);
