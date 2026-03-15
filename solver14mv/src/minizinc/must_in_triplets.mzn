predicate must_in_triplets(int: m, int: n, array [int, int] of var bool: mines) = let {
  array [int] of tuple(-2..2, -2..2, -2..2, -2..2): arr = [
    (-2, -2, -1, -1), (-2, 0, -1, 0), (-2, 2, -1, 1), (0, 2, 0, 1),
    (2, 2, 1, 1), (2, 0, 1, 0), (2, -2, 1, -1), (0, -2, 0, -1),
    (-1, -1, 1, 1), (-1, 0, 1, 0), (-1, 1, 1, -1), (0, 1, 0, -1),
  ];
} in forall(i in 1..m, j in 1..n)(
  mines[i, j] -> exists(k in 1..12)(
    let {
      int: i1 = i + arr[k].1;
      int: j1 = j + arr[k].2;
      int: i2 = i + arr[k].3;
      int: j2 = j + arr[k].4;
    } in (i1 > 0) /\ (i1 <= m) /\ (j1 > 0) /\ (j1 <= n) /\
      (i2 > 0) /\ (i2 <= m) /\ (j2 > 0) /\ (j2 <= n) /\
      mines[i1, j1] /\ mines[i2, j2]
  )
);
