predicate no_triplets(int: m, int: n, array [int, int] of var bool: mines) = let {
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
