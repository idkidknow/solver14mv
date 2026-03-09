include "def.mzn";

predicate mines_neighboring_4_le_2(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
} in forall(i in 1..m, j in 1..n)(
  let {
    var bool: n4le2 = sum(kl in [(i - 1, j), (i, j - 1), (i, j + 1), (i + 1, j)] where
      (kl.1 > 0) /\ (kl.1 <= m) /\ (kl.2 > 0) /\ (kl.2 <= n)
    )(mines[kl.1, kl.2]) <= 2;
  } in mines[i, j] -> n4le2
);
