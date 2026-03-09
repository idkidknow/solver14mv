include "def.mzn";

predicate clue_eq_neighboring_8(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  array [int, int] of -2..8: clues = board.clues;
} in forall(i in 1..m, j in 1..n)(
  if clues[i, j] >= 0 then
    clues[i, j] = sum(di, dj in {-1, 0, 1} where
      (i + di > 0) /\ (i + di <= m) /\
        (j + dj > 0) /\ (j + dj <= n) /\
        (i != 0 \/ j != 0)
    )(mines[i + di, j + dj])
  else true endif
);
