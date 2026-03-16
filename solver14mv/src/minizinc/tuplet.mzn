predicate no_triplets_of_mines(int: m, int: n, array [int, int] of var bool: mines) = let {
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

predicate no_skew_duplets_of_mines(int: m, int: n, array [int, int] of var bool: mines) = forall(i in 1..m, j in 1..n)(
  mines[i, j] -> forall(kl in [(i - 1, j - 1), (i - 1, j + 1), (i + 1, j - 1), (i + 1, j + 1)] where
    (kl.1 > 0) /\ (kl.1 <= m) /\ (kl.2 > 0) /\ (kl.2 <= n))(not(mines[kl.1, kl.2]))
);

predicate no_horizontal_quintuplets_of_mines(int: m, int: n, array [int, int] of var bool: mines) = forall(i in 1..m, j in 1..n - 4)(
  not(mines[i, j] /\ mines[i, j + 1] /\ mines[i, j + 2] /\ mines[i, j + 3] /\ mines[i, j + 4])
);

predicate no_vertical_quintuplets_of_mines(int: m, int: n, array [int, int] of var bool: mines) = forall(i in 1..m - 4, j in 1..n)(
  not(mines[i, j] /\ mines[i + 1, j] /\ mines[i + 2, j] /\ mines[i + 3, j] /\ mines[i + 4, j])
);

predicate no_horizontal_duplets_of_mines(int: m, int: n, array [int, int] of var bool: mines) = forall(i in 1..m, j in 1..n)(
  mines[i, j] -> forall(kl in [(i, j - 1), (i, j + 1)] where
    (kl.1 > 0) /\ (kl.1 <= m) /\ (kl.2 > 0) /\ (kl.2 <= n))(not(mines[kl.1, kl.2]))
);

predicate no_knights_move_duplets_of_mines(int: m, int: n, array [int, int] of var bool: mines) = forall(i in 1..m, j in 1..n)(
  mines[i, j] -> forall(kl in [(i - 1, j - 2), (i - 2, j - 1), (i - 2, j + 1), (i - 1, j + 2),
    (i + 1, j + 2), (i + 2, j + 1), (i + 2, j - 1), (i + 1, j - 2)] where
      (kl.1 > 0) /\ (kl.1 <= m) /\ (kl.2 > 0) /\ (kl.2 <= n))(not(mines[kl.1, kl.2]))
);
