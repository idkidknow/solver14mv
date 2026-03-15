include "def.mzn";
include "alldifferent.mzn";
include "increasing.mzn";

predicate clue_is_consecutive_lengths(CellContext: ctx, array [1..4] of var 0..8: lengths, array [int, int] of var bool: mines) = let {
  int: m = ctx.m;
  int: n = ctx.n;
  int: i = ctx.i;
  int: j = ctx.j;
  array [0..7] of var bool: n8 = array1d(0..7,
    [ if kl.1 > 0 /\ kl.1 <= m /\ kl.2 > 0 /\ kl.2 <= n then mines[kl.1, kl.2] else false endif |
      kl in [(i - 1, j - 1), (i - 1, j), (i - 1, j + 1), (i, j + 1), (i + 1, j + 1), (i + 1, j), (i + 1, j - 1), (i, j - 1)]
    ]
  );
  array [1..4] of var 0..7: pre0;
  var bool: match = forall(idx in 1..4)(
    let {
      var 0..8: length = lengths[idx];
      var 0..7: pre0idx = pre0[idx];
    } in (length != 0 /\ length != 8) -> (
      not(n8[pre0idx]) /\ forall(d in 1..length)(n8[(pre0idx + d) mod 8]) /\ not(n8[(pre0idx + length + 1) mod 8])
    )
  );
} in alldifferent(pre0) /\ match /\ sum(n8) = sum(lengths);

predicate clue_eq_consecutive_group_count(CellContext: ctx, 0..4: value, array [int, int] of var bool: mines) = let {
  array [1..4] of var 0..8: lengths;
} in increasing(lengths) /\ sum(idx in 1..4)(lengths[idx] > 0) = value /\ clue_is_consecutive_lengths(ctx, lengths, mines);

predicate clue_eq_longest_consecutive_length(CellContext: ctx, 0..8: value, array [int, int] of var bool: mines) = let {
  array [1..4] of var 0..8: lengths;
} in increasing(lengths) /\ lengths[4] = value /\ clue_is_consecutive_lengths(ctx, lengths, mines);
