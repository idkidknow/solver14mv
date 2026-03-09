include "def.mzn";
include "connected.mzn";

function tuple(array [int] of int, array [int] of int): graph_8_connected(int: m, int: n) = let {
  array [int] of tuple(1..m, 1..n, 1..m, 1..n): arr =
    [(i, j, i + dij.1, j + dij.2) | i in 1..m - 1, j in 1..n - 1, dij in [(0, 1), (1, 0), (1, 1)]] ++
      [(i, j, i - 1, j + 1) | i in 2..m, j in 1..n - 1] ++
      [(m, j, m, j + 1) | j in 1..n - 1] ++ [(i, n, i + 1, n) | i in 1..m - 1];
  array [int] of int: from = [(ijkl.1 - 1) * n + ijkl.2 | ijkl in arr];
  array [int] of int: to = [(ijkl.3 - 1) * n + ijkl.4 | ijkl in arr];
} in (from, to);

predicate mines_8_connected(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  tuple(array [int] of int, array [int] of int): from_to = graph_8_connected(m, n);
  array [int] of var bool: mines_flat = [mines[i, j] | i in 1..m, j in 1..n];
  array [index_set(from_to.1)] of var bool: es;
} in connected(from_to.1, from_to.2, mines_flat, es);
