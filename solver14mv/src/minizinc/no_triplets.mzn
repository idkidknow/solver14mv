include "def.mzn";

predicate no_triplets(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  var bool: vertical = forall(i in 2..m - 1, j in 1..n)(
    mines[i - 1, j] + mines[i, j] + mines[i + 1, j] <= 2
  );
  var bool: horizontal = forall(i in 1..m, j in 2..n - 1)(
    mines[i, j - 1] + mines[i, j] + mines[i, j + 1] <= 2
  );
  var bool: slash = forall(i in 2..m - 1, j in 2..n - 1)(
    mines[i - 1, j - 1] + mines[i, j] + mines[i + 1, j + 1] <= 2 /\
      mines[i + 1, j - 1] + mines[i, j] + mines[i - 1, j + 1] <= 2
  );
} in vertical /\ horizontal /\ slash;
