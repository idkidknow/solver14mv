include "def.mzn";
include "reachable.mzn";
include "connected.mzn";

function tuple(array [int] of int, array [int] of int): graph_4_connected(int: m, int: n) = let {
  array [int] of tuple(1..m, 1..n, 1..m, 1..n): arr =
    [(i, j, i + 1, j) | i in 1..m - 1, j in 1..n] ++
      [(i, j, i, j + 1) | i in 1..m, j in 1..n - 1];
  array [int] of int: from = [(ijkl.1 - 1) * n + ijkl.2 | ijkl in arr];
  array [int] of int: to = [(ijkl.3 - 1) * n + ijkl.4 | ijkl in arr];
} in (from, to);

function tuple(array [int] of int, array [int] of int): graph_4_connected_outside(int: m, int: n) = let {
  int: outside_i = m;
  int: outside_j = n + 1;
  array [int] of tuple(1..m, 1..n, 1..m, 1..n + 1): arr =
    [(i, j, i + 1, j) | i in 1..m - 1, j in 1..n] ++
      [(i, j, i, j + 1) | i in 1..m, j in 1..n - 1] ++
      [(i, 1, outside_i, outside_j) | i in 1..m] ++
      [(i, n, outside_i, outside_j) | i in 1..m] ++
      [(1, j, outside_i, outside_j) | j in 2..n - 1] ++
      [(m, j, outside_i, outside_j) | j in 2..n - 1];
  array [int] of int: from = [(ijkl.1 - 1) * n + ijkl.2 | ijkl in arr];
  array [int] of int: to = [(ijkl.3 - 1) * n + ijkl.4 | ijkl in arr];
} in (from, to);

predicate mines_4_connected(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  tuple(array [int] of int, array [int] of int): from_to = graph_4_connected(m, n);
  array [int] of var bool: mines_flat = [mines[i, j] | i in 1..m, j in 1..n];
  array [index_set(from_to.1)] of var bool: es;
} in connected(from_to.1, from_to.2, mines_flat, es);

predicate mines_4_connected_to_outside(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  tuple(array [int] of int, array [int] of int): from_to = graph_4_connected_outside(m, n);
  array [int] of var bool: mines_flat = [mines[i, j] | i in 1..m, j in 1..n] ++ [true];
  array [index_set(from_to.1)] of var bool: es;
} in connected(from_to.1, from_to.2, mines_flat, es);

predicate non_mines_4_connected(BoardData: board, array [int, int] of var bool: mines) = let {
  int: m = board.m;
  int: n = board.n;
  tuple(array [int] of int, array [int] of int): from_to = graph_4_connected(m, n);
  array [int] of var bool: non_mines_flat = [not(mines[i, j]) | i in 1..m, j in 1..n];
  array [index_set(from_to.1)] of var bool: es;
} in connected(from_to.1, from_to.2, non_mines_flat, es);
