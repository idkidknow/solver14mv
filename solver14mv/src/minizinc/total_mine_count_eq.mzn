predicate total_mine_count_eq(int: m, int: n, array [int, int] of var bool: mines, int: cnt) =
  sum(i in 1..m, j in 1..n)(mines[i, j]) = cnt;
