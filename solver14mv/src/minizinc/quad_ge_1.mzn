predicate quad_ge_1(int: m, int: n, array [int, int] of var bool: mines) =
  forall(i in 1..m - 1, j in 1..n - 1)(
    sum([mines[i, j], mines[i, j + 1], mines[i + 1, j], mines[i + 1, j + 1]]) >= 1
  );
