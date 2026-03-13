predicate same_row_sum(int: m, int: n, array [int, int] of var bool: mines) = let {
  var int: sum1 = sum(j in 1..n)(mines[1, j]);
} in forall(i in 2..m)(
  sum(j in 1..n)(mines[i, j]) = sum1
);

predicate same_column_sum(int: m, int: n, array [int, int] of var bool: mines) = let {
  var int: sum1 = sum(i in 1..m)(mines[i, 1]);
} in forall(j in 2..n)(
  sum(i in 1..m)(mines[i, j]) = sum1
);

predicate same_row_column_sum(int: m, int: n, array [int, int] of var bool: mines) =
  same_row_sum(m, n, mines) /\
    same_column_sum(m, n, mines) /\
    sum(j in 1..n)(mines[1, j]) = sum(i in 1..m)(mines[i, 1]);
