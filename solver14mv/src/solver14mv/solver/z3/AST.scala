package solver14mv.solver.z3

import scala.scalajs.js.JSConverters.*

opaque type AST = raw.AST

object AST {
  def const[S: ToSymbol](symbol: S, sort: Sort)(using ctx: Context): AST =
    raw.z3.mk_const(
      ctx.getRaw,
      ToSymbol[S].toSymbol(symbol).getRaw,
      sort.getRaw,
    )

  def int(n: Int)(using ctx: Context): AST =
    raw.z3.mk_int(ctx.getRaw, n, Sort.int.getRaw)

  def bool(b: Boolean)(using ctx: Context): AST =
    if (b) raw.z3.mk_true(ctx.getRaw) else raw.z3.mk_false(ctx.getRaw)

  def ite[A: ToAST, B: ToAST, C: ToAST](cond: A, t: B, f: C)(using
      ctx: Context
  ): AST =
    raw.z3.mk_ite(
      ctx.getRaw,
      ToAST[A].toAST(cond).getRaw,
      ToAST[B].toAST(t).getRaw,
      ToAST[C].toAST(f).getRaw,
    )

  def select[A: ToAST, B: ToAST](arr: A, idx: B)(using ctx: Context): AST =
    raw.z3.mk_select(
      ctx.getRaw,
      ToAST[A].toAST(arr).getRaw,
      ToAST[B].toAST(idx).getRaw,
    )

  def selectN[A: ToAST, B: ToAST](arr: A, idxs: Seq[B])(using
      ctx: Context
  ): AST =
    raw.z3.mk_select_n(
      ctx.getRaw,
      ToAST[A].toAST(arr).getRaw,
      idxs.map(ToAST[B].toAST(_).getRaw).toJSArray,
    )

  def pbeq[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using
      ctx: Context
  ): AST =
    raw.z3.mk_pbeq(
      ctx.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
      coeffs.toJSArray,
      k,
    )

  def pble[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using
      ctx: Context
  ): AST =
    raw.z3.mk_pble(
      ctx.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
      coeffs.toJSArray,
      k,
    )

  def pbge[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using
      ctx: Context
  ): AST =
    raw.z3.mk_pbge(
      ctx.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
      coeffs.toJSArray,
      k,
    )

  def app[A: ToAST](f: FuncDecl, args: A*)(using ctx: Context): AST =
    raw.z3.mk_app(
      ctx.getRaw,
      f.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
    )

  def not[A: ToAST](a: A)(using ctx: Context): AST =
    raw.z3.mk_not(ctx.getRaw, ToAST[A].toAST(a).getRaw)

  def and[A: ToAST](as: A*)(using ctx: Context): AST =
    raw.z3.mk_and(ctx.getRaw, as.map(ToAST[A].toAST(_).getRaw).toJSArray)

  def or[A: ToAST](as: A*)(using ctx: Context): AST =
    raw.z3.mk_or(ctx.getRaw, as.map(ToAST[A].toAST(_).getRaw).toJSArray)

  def implies[A: ToAST, B: ToAST](a: A, b: B)(using ctx: Context): AST =
    raw.z3.mk_implies(ctx.getRaw, ToAST[A].toAST(a), ToAST[B].toAST(b))

  def eq[A: ToAST, B: ToAST](a: A, b: B)(using ctx: Context): AST =
    raw.z3.mk_eq(ctx.getRaw, ToAST[A].toAST(a), ToAST[B].toAST(b))

  def forall[A: ToSymbol](vars: Seq[(A, Sort)], body: Seq[AST] => AST)(using
      ctx: Context
  ): AST = {
    val n = vars.size
    val boundVars =
      Seq.tabulate(n) { i =>
        raw.z3.mk_bound(ctx.getRaw, n - 1 - i, vars(i)._2.getRaw)
      }
    raw.z3.mk_forall(
      ctx.getRaw,
      0,
      Seq.empty.toJSArray,
      vars.map(_._2.getRaw).toJSArray,
      vars.map { case (sym, _) => ToSymbol[A].toSymbol(sym).getRaw }.toJSArray,
      body(boundVars),
    )
  }

  def forall[A: ToSymbol](var1: (A, Sort), body: AST => AST)(using
      ctx: Context
  ): AST =
    forall(Seq(var1), astSeq => body(astSeq.head))

  def forall[A: ToSymbol, B: ToSymbol](
      var1: (A, Sort),
      var2: (B, Sort),
      body: (AST, AST) => AST,
  )(using
      ctx: Context
  ): AST = {
    val v1 = (ToSymbol[A].toSymbol(var1._1), var1._2)
    val v2 = (ToSymbol[B].toSymbol(var2._1), var2._2)
    forall(Seq(v1, v2), astSeq => body(astSeq(0), astSeq(1)))
  }

  def forall[A: ToSymbol, B: ToSymbol, C: ToSymbol](
      var1: (A, Sort),
      var2: (B, Sort),
      var3: (C, Sort),
      body: (AST, AST, AST) => AST,
  )(using
      ctx: Context
  ): AST = {
    val v1 = (ToSymbol[A].toSymbol(var1._1), var1._2)
    val v2 = (ToSymbol[B].toSymbol(var2._1), var2._2)
    val v3 = (ToSymbol[C].toSymbol(var3._1), var3._2)
    forall(Seq(v1, v2, v3), astSeq => body(astSeq(0), astSeq(1), astSeq(2)))
  }

  def forall[A: ToSymbol, B: ToSymbol, C: ToSymbol, D: ToSymbol](
      var1: (A, Sort),
      var2: (B, Sort),
      var3: (C, Sort),
      var4: (D, Sort),
      body: (AST, AST, AST, AST) => AST,
  )(using
      ctx: Context
  ): AST = {
    val v1 = (ToSymbol[A].toSymbol(var1._1), var1._2)
    val v2 = (ToSymbol[B].toSymbol(var2._1), var2._2)
    val v3 = (ToSymbol[C].toSymbol(var3._1), var3._2)
    val v4 = (ToSymbol[D].toSymbol(var4._1), var4._2)
    forall(
      Seq(v1, v2, v3, v4),
      astSeq => body(astSeq(0), astSeq(1), astSeq(2), astSeq(3)),
    )
  }

  def add[A: ToAST](as: A*)(using ctx: Context): AST =
    raw.z3.mk_add(ctx.getRaw, as.map(ToAST[A].toAST(_).getRaw).toJSArray)

  def mul[A: ToAST](as: A*)(using ctx: Context): AST =
    raw.z3.mk_mul(ctx.getRaw, as.map(ToAST[A].toAST(_).getRaw).toJSArray)

  def le[A: ToAST, B: ToAST](a: A, b: B)(using ctx: Context): AST =
    raw.z3.mk_le(ctx.getRaw, ToAST[A].toAST(a), ToAST[B].toAST(b))

  def ge[A: ToAST, B: ToAST](a: A, b: B)(using ctx: Context): AST =
    raw.z3.mk_ge(ctx.getRaw, ToAST[A].toAST(a), ToAST[B].toAST(b))

  def lt[A: ToAST, B: ToAST](a: A, b: B)(using ctx: Context): AST =
    raw.z3.mk_lt(ctx.getRaw, ToAST[A].toAST(a), ToAST[B].toAST(b))

  def gt[A: ToAST, B: ToAST](a: A, b: B)(using ctx: Context): AST =
    raw.z3.mk_gt(ctx.getRaw, ToAST[A].toAST(a), ToAST[B].toAST(b))

  extension (ast: AST) {
    def getRaw: raw.AST = ast
    def stringify(using ctx: Context): String =
      raw.z3.ast_to_string(ctx.getRaw, ast)

    def &&[A: ToAST](other: A)(using ctx: Context): AST =
      and(ast, ToAST[A].toAST(other))

    def ||[A: ToAST](other: A)(using ctx: Context): AST =
      or(ast, ToAST[A].toAST(other))

    def ==>[A: ToAST](other: A)(using ctx: Context): AST =
      implies(ast, other)

    def ===[A: ToAST](other: A)(using ctx: Context): AST =
      eq(ast, other)

    def +[A: ToAST](other: A)(using ctx: Context): AST =
      add(ast, ToAST[A].toAST(other))

    def *[A: ToAST](other: A)(using ctx: Context): AST =
      mul(ast, ToAST[A].toAST(other))

    def <=[A: ToAST](other: A)(using ctx: Context): AST =
      le(ast, other)

    def >=[A: ToAST](other: A)(using ctx: Context): AST =
      ge(ast, other)

    def <[A: ToAST](other: A)(using ctx: Context): AST =
      lt(ast, other)

    def >[A: ToAST](other: A)(using ctx: Context): AST =
      gt(ast, other)
  }
}
